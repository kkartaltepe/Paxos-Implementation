package edu.utexas.kkartal.paxos;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Proposer<T extends Proposal> {
    /**
     * Send a prepare to a quorum of acceptors for the given proposal
     * @param prepareNum
     * @param quorum
     */
    void prepareFor(int prepareNum, List<Participant> quorum);

    /**
     * Handle getting a response from an accepter after sending a prepare
     * @param response
     * @param responder
     */
    void handlePrepareResponse(T response, Participant responder);

    /**
     * Send a proposal to a quorum of participants.
     * @param proposal
     * @param quorum
     */
    void propose(T proposal, List<Participant> quorum);
}
