package edu.utexas.kkartal.chat.server;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import edu.utexas.kkartal.chat.shared.PaxosMessage;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultNode {
    int port;
    int numServers;
    int firstPort;
    DatagramSocket socket;

    int preparedFor = 0;
    List<PaxosMessage<ChatMessage>> acceptedProposals;

    public static void main(String[] args) {
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();

        acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
        acceptor.getFilterChain().addFirst("object-serializer", new ProtocolCodecFilter(new ObjectSerializationCodecFactory(ClassLoader.getSystemClassLoader())));

        acceptor.setHandler(new PaxosServerHandler());

        DatagramSessionConfig config = acceptor.getSessionConfig();
        config.setReuseAddress(true);
        try {
            acceptor.bind(new InetSocketAddress(2777));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }
        System.out.println("Node started");
    }

    public void run(String[] args) {
        if(args.length < 2){
            System.out.println("Please provide two arguements <port range> <serverNum starting at 0>");
        }
        acceptedProposals = new ArrayList<PaxosMessage<ChatMessage>>();
        String[] portRange = args[0].split("-");
        firstPort = Integer.parseInt(portRange[0]);
        numServers = Integer.parseInt(portRange[1]) - Integer.parseInt(portRange[0]);
        port = Integer.parseInt(args[1]) + firstPort;

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
