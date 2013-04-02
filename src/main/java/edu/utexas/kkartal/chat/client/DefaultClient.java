package edu.utexas.kkartal.chat.client;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import org.apache.mina.filter.codec.serialization.ObjectSerializationInputStream;
import org.apache.mina.filter.codec.serialization.ObjectSerializationOutputStream;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultClient implements Client {

    String server = "localhost";
    int port = 2777;

    boolean running = true;
    DatagramChannel channel;
    DatagramSocket socket;
    List<ChatMessage> chatLog = new ArrayList<ChatMessage>();
    private ByteBuffer receiveBuffer;

    public static void main(String[] args) {
        DefaultClient client = new DefaultClient();
        client.run(args);
    }

    public void run(String[] args) {
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            socket = channel.socket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(new ChatMessage("me", "some text", new Date()));
        while(running) {
            receiveBuffer = ByteBuffer.wrap(new byte[2048]);
            try {
                SocketAddress server;
                do{
                    server = channel.receive(receiveBuffer);
                    if(server != null){
                        ObjectSerializationInputStream osis = new ObjectSerializationInputStream(new ByteArrayInputStream(receiveBuffer.array())); //Use mina serailizers because mina doesnt play nice with sun's
                        Object receivedMessage = osis.readObject();
                        onMessageReceived(receivedMessage);
                    }
                }while(server != null); //While we still have messages
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            handleInput();
        }
    }



    private void handleInput() {
        String input = System.console().readLine();
        if(input.equals("num"))
            System.out.println(chatLog.size());
    }

    private void onMessageReceived(Object receivedMessage) {
        if(!(receivedMessage instanceof ChatMessage))
            System.out.println("Recieved non chat message of type " + receivedMessage.getClass().getSimpleName());
        ChatMessage message = (ChatMessage)receivedMessage;
        System.out.println("[" + message.getTime() + "]" + message.getName() + " : " + message.getBody());
        chatLog.add(message);
    }


    public void sendMessage(ChatMessage m) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectSerializationOutputStream objectOutputStream = new ObjectSerializationOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(m);
            objectOutputStream.flush();
            ByteBuffer payload = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
            channel.send(payload, new InetSocketAddress(server, port));
        } catch (IOException e) {
            System.out.println("Failed to send message");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
