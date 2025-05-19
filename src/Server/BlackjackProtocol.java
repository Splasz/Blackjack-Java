package Server;


public class BlackjackProtocol {
    public String processInput(String input) {
        if (input == null) {
            return "Witaj! Napisz coś";
        }

        if (input.equalsIgnoreCase("bye")) {
            return "bye";
        }

        return "Odpowiedz serwera: " + input;
    }
}
