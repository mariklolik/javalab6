package org.example.server;

import org.example.message.MessageType;
import org.example.message.XMLMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class ClientHandler implements Runnable {
    private static final int TIMEOUT_SECONDS = 10;
    private final AtomicBoolean activeFlag;
    private ScheduledFuture<?> timeoutFuture;


    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;


    private String clientName;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.activeFlag = new AtomicBoolean(true);
        this.timeoutFuture = null;
    }

    @Override
    public void run() {
        try {
            XMLMessage firstMessage = XMLMessage.fromXMLString(in.readLine());
            assert firstMessage != null;
            System.out.printf("First message received from client: %s\n", firstMessage.getText());
            clientName = firstMessage.getSender();
            System.out.printf("Client connected: %s\n", clientName);
            Server.broadcastMessage(firstMessage);
            Server.broadcastUserList();

            startTimeoutTimer();

            while (activeFlag.get()) {
                XMLMessage message = XMLMessage.fromXMLString(in.readLine());
                assert message != null;
                System.out.printf("Message received from client: %s %s\n", clientName, message.getText());
                Server.broadcastMessage(message);
                resetTimeoutTimer();
            }
        } catch (SocketException e) {
            System.err.println("Client disconnected: " + e.getMessage());
            handleClientDisconnection();
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

    private void resetTimeoutTimer() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
        }
        startTimeoutTimer();
    }

    private void startTimeoutTimer() {
        timeoutFuture = Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (activeFlag.compareAndSet(true, false)) {
                System.err.println("Client timeout: " + clientName);
                XMLMessage message = new XMLMessage("Server", MessageType.POST_LOGOUT, "Client timeout: " + clientName);
                Server.deleteUser(clientSocket);
                Server.broadcastUserList();
                Server.broadcastMessage(message);
                sendMessage(new XMLMessage(clientName, MessageType.USER_KILL, ""));

                handleClientDisconnection();
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void handleClientDisconnection() {
        if (activeFlag.compareAndSet(true, false)) {
            Server.deleteUser(clientSocket);
            XMLMessage message = new XMLMessage("Server", MessageType.POST_LOGOUT, "User logout: " + clientName);
            Server.broadcastMessage(message);
            Server.broadcastUserList();
            try {
                closeResources();
            } catch (IOException ex) {
                System.out.println("Error while closing resources: " + ex.getMessage());
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

    public Socket getClientSocket() {
        return clientSocket;
    }

    public String getClientName() {
        return clientName;
    }
}
