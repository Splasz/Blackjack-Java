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
    public PrintWriter getWriter() {
        return out;
    }
    public Player getPlayer() {
        return player;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String name = in.readLine();
            player = new Player(name);
            out.println("Witaj " + player.getPlayerName());

            synchronized (BlackjackServer.class){
                BlackjackServer.players.add(player);
                BlackjackServer.clientHandlers.add(this);
            }

            BlackjackServer.blackjackProtocol.runGame(player, in, out);

        } catch (IOException e) {
            System.err.println("Błąd klienta.");
        } finally {
            synchronized (BlackjackServer.class) {
                if (player != null) {
                    BlackjackServer.players.remove(player);
                    BlackjackServer.clientHandlers.remove(this);
                    System.out.println("Gracz " + player.getPlayerName() + " opuścił grę.");
                }
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Błąd przy zamykaniu połączenia.");
                }
            }
        }
    }
}
