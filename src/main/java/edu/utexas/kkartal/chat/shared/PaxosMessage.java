package edu.utexas.kkartal.chat.shared;

import edu.utexas.kkartal.paxos.Proposal;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/31/13
 * Time: 1:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaxosMessage<T extends Serializable> implements Proposal{

    int id;
    T value;

    public PaxosMessage(int id, T value){
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public T getValue() {
        return value;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
