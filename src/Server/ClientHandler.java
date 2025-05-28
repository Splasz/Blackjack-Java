package Server;

import Game.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Player player;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Witaj! Podaj swój nick:");
            String name = in.readLine();
            player = new Player(name);

            BlackjackServer.players.add(player);
            out.println("Czekaj na swoją turę...");

            BlackjackServer.blackjackProtocol.startGame(out);
            BlackjackServer.blackjackProtocol.waitTurn(player, in, out);

        } catch (IOException e) {
            System.err.println("Błąd klienta.");
        }
    }
}
