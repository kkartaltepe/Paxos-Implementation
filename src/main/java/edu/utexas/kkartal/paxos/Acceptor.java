package edu.utexas.kkartal.paxos;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Acceptor<T extends PaxosMessage> {
    /**
     * Deal with a promise request from a proposer
     *
     * @param prepareNum
     * @param proposerId
     * @return  If the requested prepare is higher than the previous we respond with all previously accepted proposals
     * otherwise we ignore the proposer.
     */
    void handlePrepare(int prepareNum, short proposerId);

    /**
     * Handle an accept request from requester
     * @param proposal
     */
    void handlePropose(T proposal);


}
