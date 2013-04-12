package edu.utexas.kkartal.chat.server;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import edu.utexas.kkartal.chat.shared.DefaultPaxosMessage;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
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

    public static void main(String[] args) {
        if(args.length < 3)
            System.out.println("java [id] [startPort] [numServers]");
        short id = Short.parseShort(args[0]);
        short startPort = Short.parseShort(args[1]);
        short numServers = Short.parseShort(args[2]);
        ServerSet servers = new ServerSet(startPort, numServers);

        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();

        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addFirst("object-serializer", new ProtocolCodecFilter(new ObjectSerializationCodecFactory(ClassLoader.getSystemClassLoader())));

        try {
            acceptor.setHandler(new PaxosServerHandler(id, servers, DatagramChannel.open()));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.out.println("Couldnt configure nonblocking UDP channel");
            System.exit(1);
        }

        DatagramSessionConfig config = acceptor.getSessionConfig();
        config.setReuseAddress(true);
        try {
            acceptor.bind(servers.getServer(id));
            System.out.println("Server started on " + servers.getServer(id));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            System.exit(1);
        }
    }
}
