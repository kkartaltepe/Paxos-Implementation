package edu.utexas.kkartal.chat.client;

import edu.utexas.kkartal.chat.shared.ChatMessage;
import org.apache.mina.filter.codec.serialization.ObjectSerializationInputStream;
import org.apache.mina.filter.codec.serialization.ObjectSerializationOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 1:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultClient implements Client {

    String serverHost = "localhost";
    int serverPort = 2777;

    boolean running = true;
    DatagramChannel channel;
    DatagramSocket socket;
    MulticastSocket receiveSocket;
    List<ChatMessage> chatLog = new ArrayList<ChatMessage>();

    public static void main(String[] args) {
        DefaultClient client = new DefaultClient();
        client.run(args);
    }

    public void run(String[] args) {
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            socket = channel.socket();
            receiveSocket = new MulticastSocket(2666);
            receiveSocket.joinGroup(InetAddress.getByName("233.233.233.233"));
            new Thread()
            {
                public void run() {
                    try {
                        while(running){   //While we are running
                            DatagramPacket recieve = new DatagramPacket(new byte[2048], 2048);
                            receiveSocket.receive(recieve);
                            if(recieve.getLength() > 0){
                                ObjectSerializationInputStream osis = new ObjectSerializationInputStream(new ByteArrayInputStream(recieve.getData())); //Use mina serailizers because mina doesnt play nice with sun's
                                Object receivedMessage = null;
                                receivedMessage = osis.readObject();
                                onMessageReceived(receivedMessage);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }.start();  //Start listening for server responses
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Do input handling
        sendMessage(new ChatMessage("me", "some text", new Date()));
        while(running) {
            handleInput();
        }
    }



    private void handleInput() {
        String input = new Scanner(System.in).nextLine();
        if(input.equals("num"))
            System.out.println(chatLog.size());
        if(!input.isEmpty())
            sendMessage(new ChatMessage("Client", input, new Date()));
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
            channel.send(payload, new InetSocketAddress(serverHost, serverPort));
        } catch (IOException e) {
            System.out.println("Failed to send message");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
