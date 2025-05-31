package Server;

import Game.Croupier;
import Game.Deck;
import Game.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class BlackjackProtocol {
    public static Croupier croupier = new Croupier();
    public static Deck deck = new Deck();
    private int PlayerIndex = 0;

    public BlackjackProtocol() {
        deck.shuffle();
    }

    public synchronized void startGame() {
        BlackjackServer.gameStarted = true;

        for (int i = 0; i < 2; i++) {
            for (Player player : BlackjackServer.players) {
                player.addCard(deck.draw());
            }
            croupier.addCard(deck.draw());
        }

        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();

            out.println("=== START GRY ===");


            for (Player player : BlackjackServer.players) {
                out.println("Gracz: " + player.getPlayerName());
                out.println("Karty: " + player.getHandString());
                out.println();
            }
            out.println("Krupier: " + croupier.getVisibleCards());
            out.flush();
        }
    }


    public synchronized void waitTurn(Player player, BufferedReader in, PrintWriter out) throws IOException {
        while (!BlackjackServer.players.get(PlayerIndex).equals(player)) {
            try {
                out.println("Czekaj na swoją ture");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        out.println();
        out.println("Twoja tura!");
        out.println(player.getHandString());
        out.println();

        out.println("Wpisz HIT(aby dobrac karte) lub STAND(aby spasować)");

        String input;
        while ((input = in.readLine()) != null) {
            if (!isBusted(player)) {
                input = input.trim().toUpperCase();
                switch (input) {
                    case "HIT":

                        player.addCard(deck.draw());
                        out.println(player.getHandString());
                        break;

                    case "STAND":
                        out.println("Spasowales");
                        nextPlayer();
                        return;
                    default:
                        out.println("Nieznana komenda");
                        break;
                }
            } else {
                out.println("Przegrałeś");
                nextPlayer();
                return;
            }

        }
        nextPlayer();
    }

    private synchronized void nextPlayer() {
        PlayerIndex = (PlayerIndex + 1) % BlackjackServer.players.size();
        notifyAll();
    }


    public synchronized boolean isAllPLayersReady() {
        for (Player player : BlackjackServer.players) {
            if (!player.isReady()) return false;
        }
        return true;
    }

    public void isPlayerReady(Player player, BufferedReader in, PrintWriter out) throws IOException {
        out.println("Wpisz START aby zacząć");
        String input;
        while ((input = in.readLine()) != null) {
            input = input.trim().toUpperCase();

            if (input.equals("START")) {
                player.setReady(true);
                break;
            } else {
                out.println("Nieznana komenda");
            }
        }

        synchronized (this) {
            notifyAll();
        }

    }

    public boolean isBusted(Player player) {
        return player.getScore() > 21;
    }
}