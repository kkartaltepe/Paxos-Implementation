package edu.utexas.kkartal.paxos;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 4/1/13
 * Time: 11:24 PM
 * To change this template use File | Settings | File Templates.
 */
public enum PaxosMessageType {
    PREPARE,PREPARE_RESP,PROPOSE, PING, RECOVERY, RECOVERY_RESP, ACCEPTED
}
