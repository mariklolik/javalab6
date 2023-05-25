package org.example.client;

import org.example.message.Message;
import org.example.message.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private JTextArea chatTextArea;
    private JTextField inputTextField;
    private Client client;

    public ClientGUI(Client client) {
        this.client = client;
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);

        // Create components
        chatTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(chatTextArea);
        inputTextField = new JTextField();
        JButton sendButton = new JButton("Send");

        // Create panel and set layout
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputTextField, BorderLayout.NORTH);
        panel.add(sendButton, BorderLayout.SOUTH);


        // Set panel to content pane
        setContentPane(panel);
        sendMessage();
        // Set action listener for the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });



        // Start a thread to continuously update the chat text area
        Thread updateThread = new Thread(() -> {
            try {
                while (true) {
                    String message = client.getMessageQueue().take();
                    appendMessage(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        updateThread.start();

        setVisible(true);
    }

    private void sendMessage() {
        String text = inputTextField.getText().trim();
        if (!text.isEmpty()) {
            Message message = new Message(client.clientId, MessageType.POST_USER_STRING, text);
            client.sendMessage(message);
            inputTextField.setText("");
        }
    }


    private void appendMessage(String message) {
        chatTextArea.append(message + "\n");
    }



}
