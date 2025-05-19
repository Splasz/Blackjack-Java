import Game.Blackjack;


public class Main {
    public static void main(String[] args) {

        Blackjack blackjack = new Blackjack();
        blackjack.addPlayer();
        blackjack.addPlayer();
        blackjack.startRound();
    }
}