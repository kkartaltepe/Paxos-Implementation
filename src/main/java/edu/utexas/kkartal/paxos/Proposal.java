package edu.utexas.kkartal.paxos;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/31/13
 * Time: 1:50 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Proposal<T extends Serializable> extends Serializable {
    int getId();
    T getValue();
}
