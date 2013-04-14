package edu.utexas.kkartal.chat.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 4/4/13
 * Time: 2:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class ServerSet {
    short startPort;
    short numServers;
    String hostname;

    public ServerSet(short startPort, short numServers) {
        this.startPort = startPort;
        this.numServers = numServers;
        this.hostname = "localhost";
    }

    /**
     * Get an InetSocketAddress to the given server.
     * @param serverId
     * @return
     */
    InetSocketAddress getServer(int serverId) {
        if(serverId > numServers || serverId < 0)
            throw new RuntimeException("Id out of range, " + serverId + " not in [" + 0 + "," + (numServers-1) +"]");
        return new InetSocketAddress(hostname, startPort+serverId);
    }

    /**
     * Get the number of servers in the set.
     * @return
     */
    int getNumServers() {
        return numServers;
    }
}
