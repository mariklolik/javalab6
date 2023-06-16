package org.example.server;

import org.example.message.XMLMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.message.MessageType.USER_LIST;

public class Server {

    private static final int PORT = 1234;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final List<XMLMessage> chatHistory = new ArrayList<>();
    private static final int CHAT_HISTORY_SIZE = 10;

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.printf("[INFO] Started at %s:%s%n", InetAddress.getLocalHost().getHostAddress(), PORT);
            System.out.println("Server started. Listening for incoming connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                //System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                sendChatHistory(clientHandler);
                //broadcastMessage(new XMLMessage("Server", MessageType.GET_USER_ADDED, "NEW USER!"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void sendChatHistory(ClientHandler clientHandler) {
        // Send the chat history as a series of messages to the client
        for (XMLMessage message : chatHistory) {
            clientHandler.sendMessage(message);
        }
    }

    public static void deleteUser(Socket clientSocket) {
        for (int i = 0; i < clients.size(); i++) {
            if (Objects.equals(clients.get(i).getClientSocket(), clientSocket)) {
                clients.remove(i);
                break;
            }
        }
    }

    public static synchronized void broadcastMessage(XMLMessage message) {
        System.out.printf("BROADCASTING [%s]: %s\n", message.getSender(), message.getText());
        message.setText(String.format("[%s]: %s", message.getSender(), message.getText()));
        addToChatHistory(message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private static void addToChatHistory(XMLMessage message) {
        chatHistory.add(message);

        // Limit the chat history size
        if (chatHistory.size() > CHAT_HISTORY_SIZE) {
            chatHistory.remove(0);
        }
    }

    public static synchronized void broadcastUserList() {

        List<String> names = new ArrayList<>();
        for (ClientHandler client : clients) {
            names.add(client.getClientName());
        }
        XMLMessage msg = new XMLMessage("Server", USER_LIST, names.toString());
        System.out.printf("BROADCASTING USER-LIST %s%n", names);
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }
}