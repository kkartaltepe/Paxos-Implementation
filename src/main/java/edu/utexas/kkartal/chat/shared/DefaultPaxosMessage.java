package edu.utexas.kkartal.chat.shared;

import edu.utexas.kkartal.paxos.PaxosMessage;
import edu.utexas.kkartal.paxos.PaxosMessageType;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/31/13
 * Time: 1:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultPaxosMessage<T extends Serializable> implements PaxosMessage<T> {
    int instanceNum;
    int proposeNum;
    short proposerId;
    PaxosMessageType type;
    T value;

    public DefaultPaxosMessage(int instanceNum, int proposeNum, short proposerId, PaxosMessageType type, T value){
        this.instanceNum = instanceNum;
        this.proposeNum = proposeNum;
        this.proposerId = proposerId;
        this.type = type;
        this.value = value;
    }


    public int getInstanceNum() {
        return instanceNum;
    }

    public int getProposeNum() {
        return proposeNum;
    }

    public short getProposerId() {
        return proposerId;
    }

    public PaxosMessageType getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public String toString() {
        return "[" + instanceNum + "," + proposeNum + "]" + type.toString() + " from " + proposerId;
    }
}
