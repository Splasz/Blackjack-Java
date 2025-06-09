package GUI;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BlackjackClientGUI extends JFrame {
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private final BlackjackGUI blackjackUI;

    public BlackjackClientGUI() {
        setTitle("GUI.Blackjack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        blackjackUI = new BlackjackGUI();
        setContentPane(blackjackUI.getMainPanel());

        connectToServer();

        blackjackUI.startButton.addActionListener(e -> {
            sendCommand("START");
            blackjackUI.startButton.setVisible(false);
            blackjackUI.wyjdzButton.setVisible(false);
            blackjackUI.hitButton.setVisible(true);
            blackjackUI.standButton.setVisible(true);
            blackjackUI.displayLog.setText("");
        });

        blackjackUI.wyjdzButton.addActionListener(e -> {
            sendCommand("QUIT");
            closeConnection();
            System.exit(0);
        });

        blackjackUI.hitButton.addActionListener(e -> sendCommand("HIT"));
        blackjackUI.standButton.addActionListener(e -> sendCommand("STAND"));

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

            String name = JOptionPane.showInputDialog(this, "Podaj swój nick:");
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
                    blackjackUI.displayLog.append("Rozłączono z serwerem.\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Błąd połączenia z serwerem.");
            System.exit(1);
        }
    }

    private void handleServerMsg(String response) {
        if (response.startsWith("CONSOLE:")) {
            String payload = response.substring("CONSOLE:".length());

            String[] parts = payload.split(";");
            Map<String, String> map = new HashMap<>();

            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    map.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim());
                }
            }

            String type = map.get("type");   // np. "CROUPIER"
            String field = map.get("field"); // np. "POINTS"
            String value = map.get("value"); // np. "17"

            if ("croupier".equalsIgnoreCase(type) && "points".equalsIgnoreCase(field)) {
                blackjackUI.CroupierPoints.setText(value);
            }
            if ("croupier".equalsIgnoreCase(type) && "cards".equalsIgnoreCase(field)) {
                blackjackUI.CroupierCards.setText(value);
            }

            if (response.contains("END")) {
                blackjackUI.hitButton.setVisible(false);
                blackjackUI.standButton.setVisible(false);
                blackjackUI.startButton.setVisible(true);
                blackjackUI.wyjdzButton.setVisible(true);
            }
        } else {
            blackjackUI.displayLog.append(response + "\n");
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
        } catch (IOException ignored) {}
    }
}
