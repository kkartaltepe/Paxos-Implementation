package edu.utexas.kkartal.chat.server;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import edu.utexas.kkartal.chat.shared.DefaultPaxosMessage;
import edu.utexas.kkartal.paxos.*;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

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
    private boolean leader = true; //TODO: change this when leader should change

    //Acceptor info
    private int preparedFor = 0;
    private int nextInstance = 0;

    //Learner info

    PaxosServerHandler(short id) {
        this.id = id;
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
                case ACCEPTED:
                    handleAccepted(paxosMessage);
                case PREPARE:
                    handlePrepare(paxosMessage);
                case PREPARE_RESP:
                    handlePrepareResponse(paxosMessage);
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
                nextPropose++;
            } else {
                //TODO: forward to leader
            }
        } else {
            throw new Exception("Unknown message type:" + message.getClass().getSimpleName());
        }
    }

    public void handlePrepare(PaxosMessage paxosMessage) {
        if(preparedFor > paxosMessage.getProposeNum()) {
            return;
        }
        preparedFor = paxosMessage.getProposeNum();
        //TODO: send response
    }

    public void handlePrepare(int prepareNum) {

    }

    @Override
    public void handlePropose(PaxosMessage proposal) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleAccepted(PaxosMessage proposal) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handlePrepareResponse(PaxosMessage response) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void propose(PaxosMessage proposal) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
