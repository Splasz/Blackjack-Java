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
                    croupierTurn();
                    endRound();

                    BlackjackServer.class.notifyAll();
                }
            }
            resetGame();
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
            String start = buildCenteredLine("START GRY", 50, '=');
            out.println(start);


            for (Player player : BlackjackServer.players) {
                out.println("Gracz: " + player.getPlayerName());
                out.println("Karty: " + player.getHandString());
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
        String turn = buildCenteredLine("TWOJA TURA", 50, '=');
        out.println(turn);
        out.println("HIT(aby dobrac karte) lub STAND(aby spasować)");

        String input;
        while ((input = in.readLine()) != null) {
            if (!isBusted(player)) {
                input = input.trim().toUpperCase();
                switch (input) {
                    case "HIT":

                        player.addCard(deck.draw());
                        out.println(player.getHandString());
                        out.println(String.format("CONSOLE:type=%s;field=CARDS;value=%s", player.getPlayerName(), player.getHandString()));
                        out.println(String.format("CONSOLE:type=%s;field=POINTS;value=%d", player.getPlayerName(), player.getScore()));
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
                out.println();
                player.setRoundResult(Player.RoundResult.LOSE);
                nextPlayer();
                return;
            }

        }
        nextPlayer();
    }

    private synchronized void endRound() {
        checkWinners();
        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();

            String centered = buildCenteredLine("KONIEC RUNDY", 50, '=');
            out.println(centered);
            out.println("Wyniki:");
            for (Player player : BlackjackServer.players) {
                out.println(player.getPlayerName() + ": " + player.getRoundResult());
                out.println(String.format("CONSOLE:type=%s;field=RESULT;value=%s", player.getPlayerName(), player.getRoundResult()));

            }
            out.println();
        }

    }

    private synchronized void nextPlayer() {
        PlayerIndex = (PlayerIndex + 1) % BlackjackServer.players.size();
        notifyAll();
    }

    private synchronized void croupierTurn() {
        croupier.setHidenCard(false);

        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter out = handler.getWriter();

            String header = buildCenteredLine("TURA KRUPIERA", 51, '=');
            out.println(header);
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
            }else {
                out.println("Karty Krupiera:");
                out.println(croupier.getVisibleCards());
            }
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

    private void resetGame() {
        BlackjackServer.finishedPlayers = 0;
        BlackjackServer.gameStarted = false;
        PlayerIndex = 0;
        croupier.setHidenCard(true);

        for (ClientHandler handler : BlackjackServer.clientHandlers) {
            PrintWriter clientOut = handler.getWriter();
            clientOut.println("CONSOLE:type=END");
            clientOut.flush();
        }

        for (Player player : BlackjackServer.players) {
            player.setRoundResult(Player.RoundResult.NULL);
            player.setReady(false);
            player.resetHand();
            player.setScore(0);
        }

        croupier.resetCards();
        croupier.setScore(0);

        synchronized (BlackjackServer.class) {
            BlackjackServer.class.notifyAll();
        }
    }

    private synchronized boolean isAllPLayersReady() {
        for (Player player : BlackjackServer.players) {
            if (!player.isReady()) return false;
        }
        return true;
    }

    private void isPlayerReady(Player player, BufferedReader in, PrintWriter out) throws IOException {
        out.println("`START` aby zacząć lub `QUIT` aby wyjść");
        String input;
        while ((input = in.readLine()) != null) {
            input = input.trim().toUpperCase();

            if (input.equals("START")) {
                player.setReady(true);
                out.println("Oczekiwanie na reszte graczy...");
                break;
            } else if (input.equals("QUIT")) {
                out.println();
            } else {
                out.println("Nieznana komenda");
            }
        }

        synchronized (BlackjackServer.class) {
            BlackjackServer.class.notifyAll();
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