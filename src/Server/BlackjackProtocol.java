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


    public void runGame(Player player, BufferedReader in, PrintWriter out) throws IOException {

        while (true) {
            isPlayerReady(player, in, out);

            synchronized (this) {
                if (!BlackjackServer.gameStarted && isAllPLayersReady()) {
                    startGame();
                }
            }

            while (!BlackjackServer.gameStarted) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            waitTurn(player, in, out);

            synchronized (BlackjackServer.class){
                BlackjackServer.finishedPlayers++;

                while (BlackjackServer.finishedPlayers < BlackjackServer.players.size()){
                    try {
                        BlackjackServer.class.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (BlackjackServer.finishedPlayers == BlackjackServer.players.size()){
                    croupierTurn(out);
                    endRound();

                    BlackjackServer.finishedPlayers = 0;
                    BlackjackServer.gameStarted = false;
                    BlackjackServer.class.notifyAll();
                }
            }



        }
    }

    private synchronized void startGame() {
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

    private synchronized void waitTurn(Player player, BufferedReader in, PrintWriter out) throws IOException {
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
                player.setRoundResult(Player.RoundResult.LOSE);
                nextPlayer();
                return;
            }

        }
        nextPlayer();

        if (PlayerIndex == BlackjackServer.players.size()) {
            out.println("Koniec rundy oto wyniki:");
            for (Player player2 : BlackjackServer.players) {
                out.println(player2.getRoundResult());
            }
        }
    }

    private synchronized void endRound() {
        checkWinners();
        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();

            out.println("=== Koniec rundy ===:");
            out.println("Wyniki:");
            for (Player player : BlackjackServer.players) {
                out.println(player.getPlayerName() + ": " + player.getRoundResult());
            }
        }

    }

    private synchronized void nextPlayer() {
        PlayerIndex = (PlayerIndex + 1) % BlackjackServer.players.size();
        notifyAll();
    }

    private synchronized void croupierTurn(PrintWriter out) {
        croupier.setHidenCard(false);
        out.println("Krupier odkrywa karte:");
        out.println(croupier.getVisibleCards());

        int points = croupier.getScore();
        if (points < 17) {
            do {
                croupier.addCard(deck.draw());
                points = croupier.getScore();
                croupier.printCards(out);
            } while (points < 16);
        }
    }

    private synchronized void checkWinners() {
        for (Player player : BlackjackServer.players) {
            if (hasBlackjack(player)) {
                if (!hasBlackjack(croupier)) {
                    player.setRoundResult(Player.RoundResult.BLACKJACK);
                } else {
                    player.setRoundResult(Player.RoundResult.DRAW);
                }
                continue;
            }

            if (hasBlackjack(croupier)) {
                player.setRoundResult(Player.RoundResult.LOSE);
                continue;
            }

            if (isBusted(player)) {
                player.setRoundResult(Player.RoundResult.LOSE);
                continue;
            }

            if (isBusted(croupier)) {
                player.setRoundResult(Player.RoundResult.WIN);
                continue;
            }
            int playerScore = player.getScore();
            int croupierScore = croupier.getScore();

            if (playerScore > croupierScore) {
                player.setRoundResult(Player.RoundResult.WIN);
            } else if (playerScore == croupierScore) {
                player.setRoundResult(Player.RoundResult.DRAW);
            } else {
                player.setRoundResult(Player.RoundResult.LOSE);
            }
        }
    }

    private synchronized boolean isAllPLayersReady() {
        for (Player player : BlackjackServer.players) {
            if (!player.isReady()) return false;
        }
        return true;
    }

    private void isPlayerReady(Player player, BufferedReader in, PrintWriter out) throws IOException {
        out.println("Wpisz `START` aby zacząć lub `QUIT` aby wyjść");
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
        out.println("Oczekiwanie na reszte graczy...");
        synchronized (this) {
            notifyAll();
        }

    }

    private boolean isBusted(Player player) {
        return player.getScore() > 21;
    }

    private boolean isBusted(Croupier croupier) {
        return croupier.getScore() > 21;
    }

    private boolean hasBlackjack(Player player) {
        return player.getHand().size() == 2 && player.getScore() == 21;
    }

    private boolean hasBlackjack(Croupier croupier) {
        return croupier.getCards().size() == 2 && croupier.getScore() == 21;
    }


}