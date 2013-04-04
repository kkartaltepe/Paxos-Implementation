package edu.utexas.kkartal.paxos;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Proposer<T extends PaxosMessage> {

    /**
     * Handle getting a response from an accepter after sending a prepare
     * @param response
     */
    void handlePrepareResponse(T response);

    /**
     * Send a proposal to a quorum of participants.
     * @param proposal
     */
    void propose(T proposal);
}
