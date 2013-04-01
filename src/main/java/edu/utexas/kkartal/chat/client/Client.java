package edu.utexas.kkartal.chat.client;

import edu.utexas.kkartal.chat.shared.ChatMessage;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Client {
    public void sendMessage(ChatMessage m);
}
