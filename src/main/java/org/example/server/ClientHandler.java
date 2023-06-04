package org.example.server;

import org.example.message.Message;
import org.example.message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Runnable {
    private static final int TIMEOUT_SECONDS = 10;

    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final AtomicBoolean activeFlag;
    private ScheduledFuture<?> timeoutFuture;
    private String clientName;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
        this.activeFlag = new AtomicBoolean(true);
        this.timeoutFuture = null;
    }

    @Override
    public void run() {
        try {
            Message firstMessage = (Message) in.readObject();
            System.out.printf("First message received from client: %s\n", firstMessage.getText());
            clientName = firstMessage.getSender();
            System.out.printf("Client connected: %s\n", clientName);
            Server.broadcastMessage(firstMessage);
            Server.broadcastUserList();

            startTimeoutTimer();

            while (activeFlag.get()) {
                Message message = (Message) in.readObject();
                System.out.printf("Message received from client: %s %s\n", clientName, message.getText());
                Server.broadcastMessage(message);
                resetTimeoutTimer();
            }
        } catch (SocketException e) {
            System.err.println("Client disconnected: " + e.getMessage());
            handleClientDisconnection();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                closeResources();
            } catch (IOException e) {
                System.err.println("Closing resources: " + e.getMessage());
            }
        }
    }

    private void startTimeoutTimer() {
        timeoutFuture = Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (activeFlag.compareAndSet(true, false)) {
                System.err.println("Client timeout: " + clientName);
                Message message = new Message("Server", MessageType.POST_LOGOUT, "Client timeout: " + clientName);
                Server.deleteUser(clientSocket);
                Server.broadcastUserList();
                Server.broadcastMessage(message);
                sendMessage(new Message(clientName, MessageType.USER_KILL, ""));

                handleClientDisconnection();
            }
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void resetTimeoutTimer() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
        }
        startTimeoutTimer();
    }

    private void handleClientDisconnection() {
        if (activeFlag.compareAndSet(true, false)) {
            Server.deleteUser(clientSocket);
            Message message = new Message("Server", MessageType.POST_LOGOUT, "User logout: " + clientName);
            Server.broadcastMessage(message);
            Server.broadcastUserList();
            try {
                closeResources();
            } catch (IOException ex) {
                System.out.println("Error while closing resources: " + ex.getMessage());
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Message was not sent: " + e.getMessage());
        }
    }

    private void closeResources() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }

    public Object getClientSocket() {
        return clientSocket;
    }

    public String getClientName() {
        return clientName;
    }
}