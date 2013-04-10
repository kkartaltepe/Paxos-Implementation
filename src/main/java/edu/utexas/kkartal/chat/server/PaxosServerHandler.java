package edu.utexas.kkartal.chat.server;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import edu.utexas.kkartal.chat.shared.DefaultPaxosMessage;
import edu.utexas.kkartal.paxos.*;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.serialization.ObjectSerializationOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int nextPropose = 0;
    private int nextInstance = 0;
    private boolean leader = true; //TODO: change this when leader should change

    //Acceptor info
    private int preparedFor = 0;
    private ArrayList<ChatMessage> accepted = new ArrayList<ChatMessage>(10);

    //Learner info
    private ArrayList<ChatMessage> chosen = new ArrayList<ChatMessage>(10);
    private Map<Integer, Integer> proposalAcceptedVotes = new HashMap<Integer, Integer>();

    //To allow for sending packets
    private DatagramChannel channel;
    private ServerSet serverSet;

    /**
     * Create a new server handler
     * @param id
     * @param servers
     * @param channel
     */
    PaxosServerHandler(short id, ServerSet servers, DatagramChannel channel) throws IOException {
        this.id = id;
        this.channel = channel;
        this.serverSet = servers;
        channel.configureBlocking(false);
    }

    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }

    public void messageReceived(IoSession ioSession, Object message) throws Exception {
        if(message instanceof DefaultPaxosMessage) {
            PaxosMessage paxosMessage = (DefaultPaxosMessage) message;
            switch(paxosMessage.getType()){
                case PROPOSE:
//                    System.out.println("handling propose");
                    handlePropose(paxosMessage);
                    break;
                case ACCEPTED:
//                    System.out.println("handling accepted");
                    handleAccepted(paxosMessage);
                    break;
                case PREPARE:
//                    System.out.println("handling prepare");
                    handlePrepare((Integer) paxosMessage.getValue(), paxosMessage.getProposerId()); //PSN and consensus instance dont matter
                    break;
                case PREPARE_RESP:
//                    System.out.println("handling prepare response");
                    handlePrepareResponse(paxosMessage);
                    break;
                default:
                    throw new Exception("received PaxosMessage of unknown type" + paxosMessage.getType().name());
            }
        } else if(message instanceof ChatMessage) { //Only clients should be sending ChatMessages
            if(leader){
                propose(new DefaultPaxosMessage(nextInstance,
                        nextPropose,
                        id,
                        PaxosMessageType.PROPOSE,
                        (ChatMessage)message));
                nextInstance++;
//                nextPropose++;
            } else {
                sendToLeader((ChatMessage) message);
            }
        } else {
            throw new Exception("Unknown message type:" + message.getClass().getSimpleName());
        }
    }

    private void sendToLeader(ChatMessage message) {

    }

    @Override
    public void handlePrepare(int prepareNum, short proposerId) {
        if(preparedFor > prepareNum) {
            return;
        }
        preparedFor = prepareNum;
        PaxosMessage message = new DefaultPaxosMessage(-1, -1, id, PaxosMessageType.PREPARE_RESP, accepted);
        sendMessage(message, id);
    }

    @Override
    public void handlePropose(PaxosMessage proposal) {
        if(preparedFor > proposal.getProposeNum())
            return;
        accepted.ensureCapacity(proposal.getInstanceNum());
        accepted.add(proposal.getInstanceNum(), (ChatMessage) proposal.getValue());
        //Send accept to all
        sendAllMessage(new DefaultPaxosMessage(proposal.getInstanceNum(),
                                               proposal.getProposeNum(),
                                               proposal.getProposerId(),
                                               PaxosMessageType.ACCEPTED,
                                               proposal.getValue()));
    }

    @Override
    public void handleAccepted(PaxosMessage proposal) {
       Integer timesAccepted = proposalAcceptedVotes.get(proposal.getInstanceNum());
        if (timesAccepted != null) {
            proposalAcceptedVotes.put(proposal.getInstanceNum(), timesAccepted++);
        } else {
            proposalAcceptedVotes.put(proposal.getInstanceNum(), 1);
        }
        //Send to all clients
        if(proposalAcceptedVotes.get(proposal.getInstanceNum()) > serverSet.getNumServers()/2) {
            //It has been chosen.
            chosen.add(proposal.getInstanceNum(), (ChatMessage) proposal.getValue());
            sendToClients((ChatMessage) proposal.getValue());
        }


    }

    @Override
    public void handlePrepareResponse(PaxosMessage response) {
        // This is a list of chat messages that the acceptor has accepted for a given instance of Paxos.
        if((response.getValue() instanceof List))
            throw new RuntimeException("Bad PrepareResponse data");

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
    public void propose(PaxosMessage proposal) {
        sendAllMessage(proposal);
    }

    private void sendAllMessage(PaxosMessage message) {
        for(short i = 0; i < serverSet.getNumServers(); i++) {
            sendMessage(message, i);
        }
    }

    private void sendMessage(PaxosMessage message, short serverId) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectSerializationOutputStream objectOutputStream = new ObjectSerializationOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            ByteBuffer payload = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(payload, serverSet.getServer(serverId));
        } catch (IOException e) {
            System.out.println("Failed to send message");
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
