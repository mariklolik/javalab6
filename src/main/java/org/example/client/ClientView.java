package org.example.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ClientView extends JFrame {
    private final JTextArea chatTextArea;
    private final JList<String> userList;
    private final JTextField inputTextField;
    private final String clientId;

    public ClientView(String clientId) {
        this.clientId = clientId;
        // Create the chat window
        setTitle("Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);

        // Create the components
        chatTextArea = new JTextArea();
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        userList = new JList<>();
        JScrollPane userScrollPane = new JScrollPane(userList);
        inputTextField = new JTextField();
        JButton sendButton = new JButton("Send");

        // Create the layout
        setLayout(new BorderLayout());
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(userScrollPane, BorderLayout.EAST);
        add(chatPanel, BorderLayout.CENTER);
        add(inputTextField, BorderLayout.SOUTH);
        add(sendButton, BorderLayout.EAST);

        // Set up the send button action listener
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    sendMessage();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Display the chat window
        setVisible(true);
    }

    public void appendMessage(String message) {
        chatTextArea.append(message + "\n");
    }

    private void sendMessage() throws InterruptedException {
        String message = inputTextField.getText().trim();
        if (!message.isEmpty()) {
//            inputQueue.put(message);
            inputTextField.setText("");
        }
    }
}