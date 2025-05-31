package Server;

import java.net.*;
import java.io.*;

import java.util.Scanner;

public class BlackjackClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 2222;

        try (Socket socket = new Socket(hostname, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Połączono z serwerem Blackjack.");

            String response;
            while ((response = in.readLine()) != null && !response.isEmpty()) {
                System.out.println(response);
                if (!in.ready()) break;
            }

            while (true) {

                String command = scanner.nextLine();
                out.println(command);

                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    if (!in.ready()) break;
                }

                if (command.equalsIgnoreCase("QUIT")) {
                    System.out.println("Zamykam połączenie.");
                    try {
                        BlackjackServer.players.removeLast();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }


                    break;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Nieznany host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Błąd połączenia: " + e.getMessage());
        }
    }
}
