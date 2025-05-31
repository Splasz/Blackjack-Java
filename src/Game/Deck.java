package Game;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
    private final ArrayList<Card> deck = new ArrayList<>();
    private final String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private final String[] suits = {"♠", "♥", "♣", "♦"};

    public void shuffle() {
        Random rand = new Random();
        for (int i = 0; i < 56; i++) {
            Card card = new Card(values[rand.nextInt(values.length)], suits[rand.nextInt(suits.length)]);
            deck.add(card);
        }
    }

    public Card draw() {
        if (deck.isEmpty()) {
            shuffle();
        }
        return deck.remove(deck.size() - 1);
    }

    public void print() {
        System.out.println(deck);
    }
}
