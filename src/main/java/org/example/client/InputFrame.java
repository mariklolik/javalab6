package org.example.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class InputFrame extends JFrame {
    private JTextField hostTextField;
    private JTextField portTextField;
    private JTextField nameTextField;
    private JButton okButton;
    private String host;
    private String port;
    private String name;

    public InputFrame() {
        setTitle("Input");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));

        JLabel hostLabel = new JLabel("Host Address:");
        hostTextField = new JTextField();
        inputPanel.add(hostLabel);
        inputPanel.add(hostTextField);

        JLabel portLabel = new JLabel("Port Address:");
        portTextField = new JTextField();
        inputPanel.add(portLabel);
        inputPanel.add(portTextField);

        JLabel nameLabel = new JLabel("Your Name:");
        nameTextField = new JTextField();
        inputPanel.add(nameLabel);
        inputPanel.add(nameTextField);

        add(inputPanel, BorderLayout.CENTER);

        okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            host = hostTextField.getText();
            port = portTextField.getText();
            name = nameTextField.getText();
            synchronized (this) {
                notifyAll(); // Notify all waiting threads that the input is available
            }
            dispose(); // Close the frame
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                synchronized (InputFrame.this) {
                    host = null;
                    port = null;
                    name = null;
                    notifyAll(); // Notify all waiting threads that the input is null (frame closed without clicking OK)
                }
            }
        });
        setVisible(true);
    }

    public synchronized String getHost() {
        // Wait until the host input is available
        while (host == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return host;
    }

    public synchronized String getPort() {
        // Wait until the port input is available
        while (port == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return port;
    }

    public synchronized String getName() {
        // Wait until the name input is available
        while (name == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return name;
    }
}
