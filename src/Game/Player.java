package Game;

import java.util.ArrayList;

public class Player {
    private ArrayList<Card> hand = new ArrayList<>();
    private int score;
    private String name;
    public enum RoundResult {
        WIN, DRAW, LOSE, BLACKJACK, NULL
    }
    private RoundResult roundResult;

    public Player(String name) {
        this.name = name;
    }

    public String getPlayerName() {
        return name;
    }
    public void setPlayerName(String playerName) {
        this.name = playerName;
    }

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public ArrayList<Card> getHand() {
        return hand;
    }
    public RoundResult getRoundResult() {
        return roundResult;
    }
    public void setRoundResult(RoundResult roundResult) {
        this.roundResult = roundResult;

    }
    public String getHandString() {
        return hand.toString() + "\nPunkty: " + score;
    }

    public void addCard(Card card) {
        hand.add(card);
        score += card.getIntValue(score);
    }
    public void printHand() {
        System.out.println(hand);
        System.out.println("Punkty: " + score);
        System.out.println();
    }
}
