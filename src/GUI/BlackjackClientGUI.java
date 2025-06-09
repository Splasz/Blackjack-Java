package GUI;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class BlackjackClientGUI extends javax.swing.JFrame {
    private final JTextArea displayArea;
    private final JButton startButton, quitButton, hitButton, standButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public BlackjackClientGUI() {
        setTitle("Blackjack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(510, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        quitButton = new JButton("Quit");
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");

        hitButton.setVisible(false);
        standButton.setVisible(false);

        buttonPanel.add(startButton);
        buttonPanel.add(quitButton);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        add(buttonPanel, BorderLayout.SOUTH);

        connectToServer();

        startButton.addActionListener(e -> {
            sendCommand("START");
            startButton.setVisible(false);
            quitButton.setVisible(false);
            hitButton.setVisible(true);
            standButton.setVisible(true);
            displayArea.setText("");
        });
        quitButton.addActionListener(e -> {
            sendCommand("QUIT");
            closeConnection();
            System.exit(0);
        });
        hitButton.addActionListener(e -> sendCommand("HIT"));
        standButton.addActionListener(e -> sendCommand("STAND"));

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BlackjackClientGUI::new);
    }

    public void connectToServer() {
        try {
            socket = new Socket("localhost", 2222);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String name = JOptionPane.showInputDialog(this, "Podaj swoj nick:");
            if (name != null && !name.trim().isEmpty()) {
                out.println(name);
            }

            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> handleServerMsg(finalResponse));
                    }
                } catch (IOException e) {
                    displayArea.append("Rozłączono z serwerem. \n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.");
            System.exit(1);
        }
    }

    private void handleServerMsg(String response) {
        if (response.startsWith("CONSOLE:")){
            if (response.contains("END")) {
                hitButton.setVisible(false);
                standButton.setVisible(false);
                startButton.setVisible(true);
                quitButton.setVisible(true);
            }
        } else {
            displayArea.append(response + "\n");
        }


    }

    private void sendCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {

        }
    }
}
