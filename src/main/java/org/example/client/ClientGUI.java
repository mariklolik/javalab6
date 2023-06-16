package org.example.client;

import org.example.message.MessageType;
import org.example.message.XMLMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ClientGUI extends JFrame {
    private final JTextArea chatTextArea;
    private final JTextField inputTextField;
    private final Client client;

    private final DefaultListModel<String> userListModel;

    public ClientGUI(Client client) {
        this.client = client;

        client.gui = this;

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

        userListModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(userListModel);

        JScrollPane userListScrollPane = new JScrollPane(userList);
        userListScrollPane.setPreferredSize(new Dimension(150, getHeight()));
        panel.add(userListScrollPane, BorderLayout.WEST);

        // Start a thread to continuously update the chat text area
        Thread updateMessageThread = new Thread(() -> {
            try {
                while (true) {
                    String message = client.getMessageQueue().take();

                    appendMessage(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        updateMessageThread.start();

        Thread updateClientsThread = new Thread(() -> {
            try {
                while (true) {
                    String message = client.getClientsQueue().take();
                    updateUsers(message);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        updateClientsThread.start();

        setVisible(true);
    }

    private void sendMessage() {
        String text = inputTextField.getText().trim();
        if (!text.isEmpty()) {
            XMLMessage message = new XMLMessage(client.clientId, MessageType.POST_USER_STRING, text);
            client.sendMessage(message);
            inputTextField.setText("");
        }
    }


    private void appendMessage(String message) {
        chatTextArea.append(message + "\n");
    }

    private java.util.List<String> stringToArray(String string) {
        string = string.substring(1, string.length() - 1);
        String[] listElements = string.split(",");
        java.util.List<String> stringList = new ArrayList<>();
        for (String element : listElements) {
            String trimmedElement = element.trim();
            stringList.add(trimmedElement);
        }
        return stringList;
    }

    private void updateUsers(String message) {
        List<String> users = stringToArray(message);
        userListModel.clear();
        for (String username : users) {
            userListModel.addElement(username + "\n");
        }


    }


}
