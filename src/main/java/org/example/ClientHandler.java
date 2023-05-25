package org.example;

import org.example.message.Message;
import org.example.message.MessageType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final BufferedReader in;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.printf("Message received from client: %s\n", line);

                // Create a Message object
                Message message = new Message("Server", MessageType.POST_USER_STRING, line);

                // Send the Message object to all connected clients
                Server.broadcastMessage(message);
            }
        } catch (SocketException e) {
            // Log the specific "Connection reset" error
            System.err.println("Client disconnected: " + e.getMessage());
            Message message = new Message("Server", MessageType.POST_LOGOUT, "user logout");
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
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void closeResources() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }
}