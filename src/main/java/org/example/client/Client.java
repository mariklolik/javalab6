package org.example.client;

import org.example.message.MessageType;
import org.example.message.XMLMessage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private final PrintWriter out;
    private final BufferedReader in;
    private final BlockingQueue<String> messageQueue;
    private final BlockingQueue<String> clientsQueue;

    public final String clientId;
    public ClientGUI gui;

    public BlockingQueue<String> getClientsQueue() {
        return clientsQueue;
    }

    public Client(Socket socket, String clientId) throws IOException {
        this.clientId = clientId;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        messageQueue = new LinkedBlockingQueue<>();
        clientsQueue = new LinkedBlockingQueue<>();
        sendMessage(new XMLMessage(this.clientId, MessageType.POST_LOGIN, "NEW USER JOINED - %s".formatted(this.clientId)));
        // Start a separate thread to continuously receive messages from the server
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    String xml = in.readLine();
                    if (xml != null) {
                        XMLMessage message = XMLMessage.fromXMLString(xml);
                        assert message != null;
                        if (message.getMessageType() == MessageType.USER_LIST) {
                            clientsQueue.put(message.getText());

                        } else if (message.getMessageType() == MessageType.USER_KILL) {
                            this.gui.dispose();
                            System.out.println("You been inactive for too much time");
                            System.exit(0);
                        } else {
                            messageQueue.put(message.getText());
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Unable to establish connection with server");
                this.gui.dispose();
                System.exit(0);
            }
        });
        receiveThread.start();
    }

    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public void sendMessage(XMLMessage message) {
        String xml = message.toXMLString();
        if (xml != null) {
            out.println(xml);
        }
    }
}
