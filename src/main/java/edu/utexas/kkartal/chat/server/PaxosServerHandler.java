package edu.utexas.kkartal.chat.server;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import edu.utexas.kkartal.chat.shared.PaxosMessage;
import edu.utexas.kkartal.paxos.Acceptor;
import edu.utexas.kkartal.paxos.Learner;
import edu.utexas.kkartal.paxos.Participant;
import edu.utexas.kkartal.paxos.Proposer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/31/13
 * Time: 11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaxosServerHandler extends IoHandlerAdapter implements Acceptor<PaxosMessage>,Learner<PaxosMessage>,Proposer<PaxosMessage> {
    private int preparedFor = 0;

    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        throwable.printStackTrace();
    }

    public void messageReceived(IoSession ioSession, Object o) throws Exception {
        if(!(o instanceof PaxosMessage) && !(o instanceof ChatMessage)) {
            throw new Exception("Received bad message (" + o.getClass().getSimpleName() + ")");
        }
        //TODO: do paxos stuff
        ioSession.write(o);
    }

    public void handlePrepare(int prepareNum, Participant requester) {
        if(preparedFor > prepareNum) {
            return;
        }
        preparedFor = prepareNum;
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
    }

    public void handleAccept(PaxosMessage proposal, Participant requester) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleAccepted(PaxosMessage proposal, Participant acceptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleQuery(Participant requester) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void prepareFor(int prepareNum, List<Participant> quorum) {
        for(Participant participant : quorum) {
            ChatMessage prepMsg = null;
            participant.send(prepMsg);
        }
    }

    public void handlePrepareResponse(PaxosMessage response, Participant responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void propose(PaxosMessage proposal, List<Participant> quorum) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
