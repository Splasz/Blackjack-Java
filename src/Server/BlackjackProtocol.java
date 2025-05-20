package Server;


public class BlackjackProtocol {
    private static final int WAITING = 0;
    private static final int START = 1;
    private static final int ANOTHER = 2;
    private int state = WAITING;

    public String processInput(String theInput) {
        String theOutput = null;

        if (state == WAITING) {
            theOutput = "Witaj w grze w Blackjacka! Napisz 'start' aby zagrac lub 'exit' aby wyjsc.";
            state = START;

        } else if (state == START) {
            if (theInput.equalsIgnoreCase("start")) {
                theOutput = "Rozpoczynamy gre. (tu logika gry...) Napisz 'again' aby zagrac ponownie lub 'exit'.";
                state = ANOTHER;
            } else if (theInput.equalsIgnoreCase("exit")) {
                theOutput = "zakoncz";
            } else {
                theOutput = "Nieznana komenda. Napisz 'start' lub 'exit'.";
            }

        } else if (state == ANOTHER) {
            if (theInput.equalsIgnoreCase("again")) {
                theOutput = "Nowa gra. (tu logika gry...) Napisz 'again' lub 'exit'.";
            } else if (theInput.equalsIgnoreCase("exit")) {
                theOutput = "zakoncz";
            } else {
                theOutput = "Nieznana komenda. Napisz 'again' lub 'exit'.";
            }
        }

        return theOutput;
    }
}
