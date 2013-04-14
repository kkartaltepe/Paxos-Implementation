package edu.utexas.kkartal.chat.server;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import edu.utexas.kkartal.chat.shared.DefaultPaxosMessage;
import edu.utexas.kkartal.paxos.*;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.serialization.ObjectSerializationOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/31/13
 * Time: 11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaxosServerHandler extends IoHandlerAdapter implements Acceptor<PaxosMessage>,Learner<PaxosMessage>,Proposer<PaxosMessage> {
    //Proposer info
    private short id;
    private int nextPropose = 1;
    private int nextInstance = 0;

    //Acceptor info
    private int preparedFor = 1;
    private ArrayList<ChatMessage> accepted = new ArrayList<ChatMessage>(10);

    //Learner info
    private ArrayList<ChatMessage> chosen = new ArrayList<ChatMessage>(10);
    private int lastSent = -1; //Havnt sent anything
    private Map<Integer, List<PaxosMessage>> proposalAcceptedVotes = new HashMap<Integer, List<PaxosMessage>>();

    //To allow for sending packets
    private DatagramChannel channel;
    private ServerSet serverSet;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(100); //Handle waiting for at most 100 proposals to be chosen eventually.
    private Date leaderLastHeard;

    /**
     * Create a new server handler
     * @param id
     * @param servers
     * @param channel
     */
    PaxosServerHandler(final short id, final ServerSet servers, DatagramChannel channel) throws IOException {
        this.id = id;
        this.channel = channel;
        this.serverSet = servers;
        this.leaderLastHeard = new Date(); //Now.
        channel.configureBlocking(false);
        /**
         * Set up the check for check for leader being alive.
         */
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Calendar timeForMeToBeLeader = new GregorianCalendar();
                int remainder = preparedFor%servers.getNumServers();
                int turnsTillLeader =  id < remainder ? id-remainder+serverSet.getNumServers() : id-remainder;
                timeForMeToBeLeader.add(Calendar.SECOND, -6*turnsTillLeader);
                if(timeForMeToBeLeader.getTime().after(leaderLastHeard) && id != getLeader()) {  //Enough time that I should be leader now.
                    System.out.println("Trying to become leader with " + (preparedFor+turnsTillLeader));
                    sendAllMessage(new DefaultPaxosMessage(-1, -1, id, PaxosMessageType.PREPARE, preparedFor+turnsTillLeader));
                }
                if(id == getLeader()) //Send everyone heartbeats
                    sendHeartBeat();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void sendHeartBeat() {
            sendAllMessage(new DefaultPaxosMessage(-1, -1, id, PaxosMessageType.PING, preparedFor));
    }

    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }

    public void messageReceived(IoSession ioSession, Object message) throws Exception {
        if(message instanceof DefaultPaxosMessage) {
            PaxosMessage paxosMessage = (DefaultPaxosMessage) message;
            switch(paxosMessage.getType()){
                case PROPOSE:
                    handlePropose(paxosMessage);
                    break;
                case ACCEPTED:
                    handleAccepted(paxosMessage);
                    break;
                case PREPARE:
                    handlePrepare((Integer) paxosMessage.getValue(), paxosMessage.getProposerId()); //PSN and consensus instance dont matter
                    break;
                case PREPARE_RESP:
                    handlePrepareResponse(paxosMessage);
                    break;
                case PING:
                    if(paxosMessage.getProposerId() == getLeader()) //Leaders HeartBeat
                        leaderLastHeard = new Date();
                    else if(((Integer)paxosMessage.getValue()) > preparedFor) { //A new leader is informing everyone of his existance
                        System.out.println("Ping informed me of new leader " + paxosMessage.getValue());
                        preparedFor = ((Integer)paxosMessage.getValue());
                    }
                    break;
                default:
                    throw new Exception("received PaxosMessage of unknown type" + paxosMessage.getType().name());
            }
        } else if(message instanceof ChatMessage) { //Some client sent or some server forwarded.
            if(id == getLeader()){
                propose(new DefaultPaxosMessage(nextInstance,
                        nextPropose,
                        id,
                        PaxosMessageType.PROPOSE,
                        (ChatMessage)message));
                nextInstance++;
            } else {
                sendToLeader((ChatMessage) message);
            }
        } else {
            throw new Exception("Unknown message type:" + message.getClass().getSimpleName());
        }
    }


    @Override
    public void handlePrepare(int prepareNum, short proposerId) {
        if(preparedFor > prepareNum) {
            return;
        }
        preparedFor = prepareNum;
        nextPropose = preparedFor;
        PaxosMessage message = new DefaultPaxosMessage(-1, -1, id, PaxosMessageType.PREPARE_RESP, accepted);
        sendToLeader(message);
    }

    @Override
    public void handlePrepareResponse(PaxosMessage response) {
        // This is a list of chat messages that the acceptor has accepted for a given instance of Paxos.
        //if((response.getValue() instanceof ArrayList))
        //    throw new RuntimeException("Bad PrepareResponse data, was expecting list but got " + response.getValue().getClass());

        List<ChatMessage> remoteAccepted = (List<ChatMessage>) response.getValue();
        for(int i = 0; i < remoteAccepted.size(); i++) {
            if(chosen.size() > i && chosen.get(i) != null)
                continue;
            if(remoteAccepted.get(i) != null) {
                propose(new DefaultPaxosMessage(i, nextPropose, id, PaxosMessageType.PROPOSE, remoteAccepted.get(i)));
                if(i > nextInstance)
                    nextInstance = i;
            }
        }
    }

    @Override
    public void handlePropose(PaxosMessage proposal) {
        if(preparedFor > proposal.getProposeNum())
            return;
        while(accepted.size() <= proposal.getInstanceNum()){
            System.out.println("Adding to chosen.");
            accepted.add(null);
            chosen.add(null);
        }
        accepted.set(proposal.getInstanceNum(), (ChatMessage) proposal.getValue());
        //Send accept to all
        sendAllMessage(new DefaultPaxosMessage(proposal.getInstanceNum(),
                                               proposal.getProposeNum(),
                                               id,
                                               PaxosMessageType.ACCEPTED,
                                               proposal.getValue()));
    }

    @Override
    public void handleAccepted(PaxosMessage proposal) {
        while(chosen.size() < proposal.getInstanceNum()+1){   //Prevent NPE from accept before proposal.
            chosen.add(null);
        }
        if(chosen.get(proposal.getInstanceNum()) != null)
            return; //Something has already been chosen so I dont care anymore.

        List<PaxosMessage> acceptedVotes = proposalAcceptedVotes.get(proposal.getInstanceNum());
        if (acceptedVotes == null) {
            acceptedVotes = new ArrayList<PaxosMessage>();
            acceptedVotes.add(proposal);
            proposalAcceptedVotes.put(proposal.getInstanceNum(), acceptedVotes);
        } else {
            for(PaxosMessage acceptedVote : acceptedVotes){
                if(proposal.getProposerId() == acceptedVote.getProposerId())
                    return;     //This person is reaffirming their acceptance of this instances proposal.
            }
            acceptedVotes.add(proposal);
            proposalAcceptedVotes.put(proposal.getInstanceNum(), acceptedVotes);
        }
        //handle being chosen
        if(proposalAcceptedVotes.get(proposal.getInstanceNum()).size() > serverSet.getNumServers()/2) {
            onChosen(proposal);
        }

        //TODO: Maybe handle different proposal numbers happeneing "simulataneously"
    }

    /**
     * Work through the chosen messages sending what we have left and
     * stopping once we get to an unfilled slot or the end of the array.
     */
    private void onChosen(PaxosMessage proposal) {
        chosen.set(proposal.getInstanceNum(), (ChatMessage) proposal.getValue());
        if(proposal.getInstanceNum() >= nextInstance)
            nextInstance = proposal.getInstanceNum()+1;

        while(lastSent+1 < chosen.size()) {
            ChatMessage toSend = chosen.get(lastSent+1);
            if(toSend != null) {
                if(id == getLeader())
                    sendToClients(toSend);
                lastSent++;
            } else {
                break; //There is still a missing slot.
            }
        }
    }

    @Override
    public void propose(final PaxosMessage proposal) {
        sendAllMessage(proposal);
        Runnable rePropose = new Runnable(){
            @Override
            public void run() {
                long timeToSleep = 1000;
                while(chosen.get(proposal.getInstanceNum()) == null){
                    try {
                        Thread.sleep(timeToSleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendAllMessage(proposal);
                    timeToSleep = timeToSleep*2 < 60*1000 ? timeToSleep*2 : 60*1000;   //Increase wait time up to a minute (helps prevent spamming in the logs >.>)
                }
                System.out.println("Chose: " + proposal);
            }
        };
        scheduler.schedule(rePropose, 1, TimeUnit.SECONDS);
    }

    private void sendAllMessage(PaxosMessage message) {
        for(short i = 0; i < serverSet.getNumServers(); i++) {
            sendMessage(message, i);
        }
    }

    private short getLeader() {
        return (short) (preparedFor%serverSet.getNumServers());
    }

    private void sendToLeader(Serializable message) {
        sendMessage(message, getLeader());
    }

    private void sendMessage(Serializable message, short serverId) {
        try {
            if(serverId == id) { //Sending to myself.
                messageReceived(null, message);
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectSerializationOutputStream objectOutputStream = new ObjectSerializationOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
                ByteBuffer payload = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
                channel.send(payload, serverSet.getServer(serverId));
            }
        } catch (IOException e) {
            System.out.println("Failed to send message");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void sendToClients(ChatMessage message) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectSerializationOutputStream objectOutputStream = new ObjectSerializationOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            ByteBuffer payload = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(payload, new InetSocketAddress("233.233.233.233", 2666)); //Special mutlicast for clients
        } catch (IOException e) {
            System.out.println("Failed to send message");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
