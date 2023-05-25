package org.example.client;

import org.example.message.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final PrintWriter out;
    private final ObjectInputStream in;

    private final String clientId;
    public Client(Socket socket, String clientId) throws InterruptedException, IOException {
        this.clientId = clientId;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new ObjectInputStream(socket.getInputStream());

        // Broadcast listener thread
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message message = (Message) obj;
                        System.out.println("Received from server: " + message.getText());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

        Scanner sc = new Scanner(System.in);
        String line;

        while (true) {
            line = sc.nextLine();

            out.println(line);
            out.flush();

            if ("exit".equalsIgnoreCase(line)) {
                break;
            }
        }

        // Wait for the receiveThread to finish before closing the socket and streams
        receiveThread.join();

    }
}
