package Server;

import Game.Player;

import java.io.BufferedReader;
import java.io.PrintWriter;

import static Server.BlackjackServer.croupier;
import static Server.BlackjackServer.deck;

public class BlackjackProtocol {
    private int PlayerIndex = 0;

    public BlackjackProtocol() {

    }

    public synchronized void startGame(PrintWriter out){
        out.println("Start");
        for (int i = 0; i < 2; i++) {
            for (Player player : BlackjackServer.players) {
                out.println("Gracz:" + player.getHandString());
                player.addCard(deck.draw());
            }
            out.println("Krupier: " + croupier.getVisibleCards());
            croupier.addCard(deck.draw());
        }
    }

    public synchronized void waitTurn(Player player, BufferedReader in, PrintWriter out) {
        while (!BlackjackServer.players.get(PlayerIndex).equals(player)) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        out.println("Twoja tura!");
        out.println(player.getHandString());


        PlayerIndex = (PlayerIndex + 1) % BlackjackServer.players.size();
        notifyAll();
    }
}