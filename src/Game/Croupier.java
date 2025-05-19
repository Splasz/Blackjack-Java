package Game;

import java.util.ArrayList;

public class Croupier {
    private ArrayList<Card> cards = new ArrayList<>();
    private int score;
    private boolean hidenCard = true;

    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public ArrayList<Card> getCards() {
        return cards; }
    public boolean isHidenCard() {
        return hidenCard;
    }
    public void setHidenCard(boolean hidenCard) {
        this.hidenCard = hidenCard;
    }

    public void addCard(Card card) {
        cards.add(card);
        score += card.getIntValue(score);
    }
    public void printCards() {
        if (hidenCard) {
            System.out.print("[");
            System.out.print(cards.get(0));
            System.out.print(", ???");
            System.out.println("]");
            System.out.println("Punkty: " + cards.get(0).getIntValue(score));
        } else {
            System.out.println(cards);
            System.out.println("Punkty: " + score);
        }
        System.out.println();
    }
}
