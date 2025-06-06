package Game;

public class Card {
    private  String value;
    private  String suit;

    Card(String value, String suit) {
        this.value = value;
        this.suit = suit;
    }

    public String getValue() {
        return value;
    }
    public String getSuit() {
        return suit;
    }
    public int getIntValue(int points){
        switch (value){
            case "2": return 2;
            case "3": return 3;
            case "4": return 4;
            case "5": return 5;
            case "6": return 6;
            case "7": return 7;
            case "8": return 8;
            case "9": return 9;
            case "10": return 10;
            case "J":
            case "Q":
            case "K":
                return 10;
            case "A":
                if (points + 11 > 21){
                    return 1;
                } else {
                    return 11;
                }
            default:
                return 0;
        }
    }
    public int getIntValue(int points, Croupier croupier){
        switch (value){
            case "2": return 2;
            case "3": return 3;
            case "4": return 4;
            case "5": return 5;
            case "6": return 6;
            case "7": return 7;
            case "8": return 8;
            case "9": return 9;
            case "10": return 10;
            case "J":
            case "Q":
            case "K":
                return 10;
            case "A":
                if (!croupier.isHidenCard()){
                    return 11;
                } else if (points + 11 > 21){
                    return 1;
                }
            default:
                return 0;
        }
    }
    @Override
    public String toString() {
        return suit + "-" + value;
    }
}
