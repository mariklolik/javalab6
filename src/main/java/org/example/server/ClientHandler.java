package org.example.server;

import org.example.message.MessageType;
import org.example.message.XMLMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            XMLMessage firstMessage = XMLMessage.fromXMLString(in.readLine());
            System.out.printf("First message received from client: %s\n", firstMessage.getText());
            clientName = firstMessage.getSender();
            System.out.printf("Client connected: %s\n", clientName);
            Server.broadcastMessage(firstMessage);

            while (true) {
                XMLMessage message = XMLMessage.fromXMLString(in.readLine());
                System.out.printf("Message received from client: %s %s\n", clientName, message.getText());
                Server.broadcastMessage(message);
            }
        } catch (SocketException e) {
            System.err.println("Client disconnected: " + e.getMessage());

            XMLMessage message = new XMLMessage("Server", MessageType.POST_LOGOUT, "User logout: " + clientName);
            Server.broadcastMessage(message);
            try {
                closeResources();
            } catch (IOException ex) {
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                closeResources();
            } catch (IOException e) {
                System.err.println("Closing resources: " + e.getMessage());
            }
        }
    }

    public void sendMessage(XMLMessage message) {
        out.println(message.toXMLString());
        out.flush();
    }

    private void closeResources() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }
}
