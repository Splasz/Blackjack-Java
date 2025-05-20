package Game;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class Blackjack {
    private final Deck deck = new Deck();
    private final ArrayList<Player> players = new ArrayList<>();
    private final Croupier croupier = new Croupier();
    private static int playersCount = 0;

    public Blackjack() {
        deck.shuffle();
    }

    public void dealCards() {
        for (int i = 0; i < 2; i++) {
            for (Player player : players) {
                dealCardToPlayer(player);
                System.out.println("Karty gracza " + player.getPlayerName() + ":");
                player.printHand();
            }
            dealCardToCroupier();
            System.out.println("Karty krupiera:");
            croupier.printCards();
            System.out.println("------------------------------");
        }
    }
    public void dealCardToPlayer(Player player) {
        player.addCard(deck.draw());
    }
    public void dealCardToCroupier() {
        croupier.addCard(deck.draw());
    }

    public void PlayerTurn(Player player) {
        Scanner sc = new Scanner(System.in);
        String playerInput = "d";

        System.out.println("Ruch Gracza " + player.getPlayerName() + ":");
        while (Objects.equals(playerInput, "d")) {
            if (!isBusted(player)) {
                System.out.println("Dobierz / Pas      (d/p)");
                playerInput = sc.nextLine();
                if (Objects.equals(playerInput, "d")) {
                    System.out.println("Gracz dobiera: ");
                    player.addCard(deck.draw());
                    player.printHand();
                } else {
                    System.out.println("Gracz pasuje");
                    break;
                }
            } else {
                return;
            }

        }
        System.out.println("------------------------");
    }
    public void croupierTurn() {
        croupier.setHidenCard(false);
        System.out.println("Krupier odkyrwa karte: ");
        croupier.printCards();
        System.out.println("----------------------");

        System.out.println("Ruch Krupiera:");
        int points = croupier.getScore();

        if (points < 17) {
            do {
                croupier.addCard(deck.draw());
                points = croupier.getScore();
                croupier.printCards();
            } while (points < 16);
        }
        System.out.println("----------------------");
    }
    public boolean isBusted(Player player) {
        return player.getScore() > 21;
    }
    public boolean isBusted(Croupier croupier) {
        return croupier.getScore() > 21;
    }

    public void endRound() {
        for (Player player : players) {
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
    public boolean hasBlackjack(Player player) {
        return player.getHand().size() == 2 && player.getScore() == 21;
    }
    public boolean hasBlackjack(Croupier croupier) {
        return croupier.getCards().size() == 2 && croupier.getScore() == 21;
    }

    public void displayPlayersHands() {
        for (Player player : players) {
            System.out.print("Karty gracza " + player.getPlayerName() + ": ");
            player.printHand();
        }
    }
    public void displayCroupierCards() {
        System.out.print("Karty krupiera: ");
        croupier.printCards();
    }
    public void displayResults() {
        for (Player player : players) {
            System.out.println("Wynik gracza " + player.getPlayerName() + ": " + player.getRoundResult());
        }
    }

    public void addPlayer() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Podaj nazwe: ");
        String name = scanner.nextLine();

        if (name == null || name.isEmpty()) {
            players.add(new Player("Gracz " + ++playersCount));
        } else {
            players.add(new Player(name));
        }
    }
    public void removePlayer(Player player) {
        players.remove(player);
    }
    public void resetPlayers() {
        for (Player player : players) {
            player.getHand().clear();
            player.setScore(0);
            player.setRoundResult(Player.RoundResult.NULL);
        }
    }

    public void startRound() {
        dealCards();
        for (Player player : players) {
            PlayerTurn(player);
        }
        croupierTurn();
        endRound();
        displayPlayersHands();
        displayCroupierCards();
        displayResults();
    }

    public void resetGame() {
        deck.shuffle();
        resetPlayers();
    }
    public void resetRound() {
        resetPlayers();
    }

    public void test() {

        dealCards();
        endRound();

        displayResults();

    }
}
