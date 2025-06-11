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
    private static int currentPlayerIndex = 0;

    public void runGame(Player player, BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            isPlayerReady(player, in, out);

            synchronized (BlackjackServer.class) {
                if (!BlackjackServer.gameStarted && isAllPLayersReady()) {
                    startGame();
                    BlackjackServer.gameStarted = true;
                    BlackjackServer.class.notifyAll();
                }
            }

            synchronized (BlackjackServer.class) {
                while (!BlackjackServer.gameStarted) {
                    try {
                        BlackjackServer.class.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            playerTurn(player, in, out);

            synchronized (BlackjackServer.class) {
                BlackjackServer.finishedPlayers++;

                if (BlackjackServer.finishedPlayers < BlackjackServer.players.size()) {
                    try {
                        BlackjackServer.class.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    croupierTurn();
                    checkWinners();
                    endRound();
                    resetGame();
                    BlackjackServer.class.notifyAll();
                }
            }
        }
    }

    private void startGame() {
        deck.shuffle();
        currentPlayerIndex = 0;

        for (int i = 0; i < 2; i++) {
            for (Player player : BlackjackServer.players) {
                player.addCard(deck.draw());
            }
            croupier.addCard(deck.draw());
        }

        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();
            String start = buildCenteredLine("START GRY", 50, '=');
            out.println(start);

            for (Player player : BlackjackServer.players) {
                out.println("Gracz: " + player.getPlayerName());
                out.println("Karty: " + player.getHandString() + " (" + player.getScore() + ")");
                out.println(String.format("CONSOLE:type=%s;field=CARDS;value=%s", player.getPlayerName(), player.getHandString()));
                out.println(String.format("CONSOLE:type=%s;field=POINTS;value=%d", player.getPlayerName(), player.getScore()));
                out.println();
            }
            out.println("Krupier: " + croupier.getVisibleCards());
            out.println("Punkty: " + croupier.getScore());
            out.println(String.format("CONSOLE:type=CROUPIER;field=CARDS;value=%s", croupier.getVisibleCards()));
            out.println(String.format("CONSOLE:type=CROUPIER;field=POINTS;value=%d", croupier.getScore()));
            out.flush();
        }
    }

    private void playerTurn(Player player, BufferedReader in, PrintWriter out) throws IOException {
        synchronized (this) {
            while (!BlackjackServer.players.get(currentPlayerIndex).equals(player)) {
                try {
                    out.println("Czekaj na swoją ture");
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        out.println();
        out.println(buildCenteredLine("TWOJA TURA", 50, '='));
        out.println("HIT - Dobierz kartę        |       STAND - Spasuj");

        while (!isBusted(player)) {
            String input = in.readLine();
            if (input == null) break;

            switch (input.trim().toUpperCase()) {
                case "HIT":
                    player.addCard(deck.draw());
                    out.println(player.getHandString());
                    out.println(String.format("CONSOLE:type=%s;field=CARDS;value=%s", player.getPlayerName(), player.getHandString()));
                    out.println(String.format("CONSOLE:type=%s;field=POINTS;value=%d", player.getPlayerName(), player.getScore()));
                    break;

                case "STAND":
                    out.println("Spasowałeś");
                    nextPlayer();
                    return;
                default:
                    out.println("Nieznana komenda");
                    break;
            }
        }

        out.println("Przegrałeś (bust)");
        player.setRoundResult(Player.RoundResult.LOSE);
        nextPlayer();
    }

    private synchronized void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % BlackjackServer.players.size();
        notifyAll();
    }

    private void croupierTurn() {
        croupier.setHidenCard(false);

        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();

            out.println(buildCenteredLine("TURA KRUPIERA", 51, '='));
            out.println(String.format("CONSOLE:type=CROUPIER;field=CARDS;value=%s", croupier.getVisibleCards()));
            out.println(String.format("CONSOLE:type=CROUPIER;field=POINTS;value=%d", croupier.getScore()));

            int points = croupier.getScore();
            if (points < 17) {
                do {
                    out.println("Krupier dobiera...");
                    croupier.addCard(deck.draw());
                    points = croupier.getScore();
                    croupier.printCards(out);
                    out.println(String.format("CONSOLE:type=CROUPIER;field=CARDS;value=%s", croupier.getVisibleCards()));
                    out.println(String.format("CONSOLE:type=CROUPIER;field=POINTS;value=%d", points));
                } while (points < 16);
            } else {
                out.println("Karty Krupiera:");
                out.println(croupier.getVisibleCards() + "  (" + points + ")");
            }
        }
    }

    private void checkWinners() {
        for (Player player : BlackjackServer.players) {
            if (isBusted(player)) {
                player.setRoundResult(Player.RoundResult.LOSE);
            } else if (hasBlackjack(player) && !hasBlackjack(croupier)) {
                player.setRoundResult(Player.RoundResult.BLACKJACK);
            } else if (isBusted(croupier)) {
                player.setRoundResult(Player.RoundResult.WIN);
            } else {
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
    }

    private void endRound() {
        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();

            out.println(buildCenteredLine("KONIEC RUNDY", 50, '='));
            out.println("Wyniki:");
            for (Player player : BlackjackServer.players) {
                out.println(player.getPlayerName() + ": " + player.getRoundResult());
                out.println(String.format("CONSOLE:type=%s;field=RESULT;value=%s", player.getPlayerName(), player.getRoundResult()));

            }
            out.println("Karty krupiera: " + croupier.getVisibleCards() + " (" + croupier.getScore() + ")");
            out.println();
        }

    }

    private void resetGame() {
        BlackjackServer.finishedPlayers = 0;
        BlackjackServer.gameStarted = false;
        croupier.resetCards();
        croupier.setHidenCard(true);
        croupier.setScore(0);

        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter clientOut = handler.getWriter();
            clientOut.println("CONSOLE:type=END");
        }

        for (Player player : BlackjackServer.players) {
            player.resetHand();
            player.setScore(0);
            player.setReady(false);
            player.setRoundResult(Player.RoundResult.NULL);
        }
    }

    private boolean isAllPLayersReady() {
        for (Player player : BlackjackServer.players) {
            if (!player.isReady()) return false;
        }
        return true;
    }

    private void isPlayerReady(Player player, BufferedReader in, PrintWriter out) throws IOException {
        out.println("START aby dołączyć do gry:");
        String input;
        while ((input = in.readLine()) != null) {
            input = input.trim().toUpperCase();

            if (input.equals("START")) {
                player.setReady(true);
                out.println("Oczekiwanie na reszte graczy...");
                break;
            } else {
                out.println("Nieznana komenda");
            }
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

    private String buildCenteredLine(String text, int totalWidth, char filler) {
        int padding = totalWidth - text.length() - 2;
        int left = padding / 2;
        int right = padding - left;

        return String.valueOf(filler).repeat(left) + " " + text + " " + String.valueOf(filler).repeat(right);
    }
}