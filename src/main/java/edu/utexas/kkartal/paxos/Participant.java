package edu.utexas.kkartal.paxos;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Participant {

    /**
     * Send a message to the quorum this object ecapsulates
     * @param message
     */
    void send(Serializable message);

    /**
     * Return the ID of this participant
     * @return
     */
    int getId();

}
