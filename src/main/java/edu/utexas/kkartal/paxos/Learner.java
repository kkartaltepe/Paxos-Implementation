package edu.utexas.kkartal.paxos;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Learner<T extends Proposal> {
    /**
     * Handle an acceptor informing you of its acceptance of a proposal
     * @param proposal
     */
    void handleAccepted(T proposal, Participant acceptor);

    /**
     * Handle someone asking if something was chosen
     * @param requester
     */
    void handleQuery(Participant requester);
}
