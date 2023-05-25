package org.example.client;

import java.io.IOException;

import java.net.Socket;


public class Main {
    public static void main(String[] args) throws IOException {

        InputFrame inputFrame = new InputFrame();

        String host = inputFrame.getHost();
        String port = inputFrame.getPort();
        String name = inputFrame.getName();

        // Use the retrieved input values as needed
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println("Name: " + name);
        Socket socket;
        try {
            socket = new Socket(host, Integer.parseInt(port));
        }
        catch (IOException e){
            System.err.printf("Unable to create connection to %s:%s%n", host, port);
            return;
        }

        Client client = new Client(socket, name);
        new ClientGUI(client);
    }

}
//        Socket socket = new Socket("localhost", 1234);
//        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
//        BlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();
//        Client client = null;
//        ClientView view = new ClientView(client);
//        client = new Client(socket, inputQueue, outputQueue);


