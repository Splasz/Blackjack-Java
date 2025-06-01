package Game;

import java.io.PrintWriter;
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
    public String getVisibleCards() {
        if (hidenCard && cards.size() > 1) {
            return "[" + cards.get(0).toString() + ", ???]\nPunkty: " + cards.get(0).getIntValue(score, this);
        } else {
            return cards.toString() + "\nPunkty: " + score;
        }
    }

    public void addCard(Card card) {
        cards.add(card);
        score += card.getIntValue(score, this);
    }
    public void printCards(PrintWriter out) {
        if (hidenCard) {
            out.print("[");
            out.print(cards.get(0));
            out.print(", ???");
            out.println("]");
            out.println("Punkty: " + cards.get(0).getIntValue(score,this));
        } else {
            out.println(cards);
            out.println("Punkty: " + score);
        }
        out.println();
    }
}
