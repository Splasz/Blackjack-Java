package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BlackjackClient2 {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("localhost", 12345);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in))
        ) {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("Serwer: " + serverMessage);
                String userInput = console.readLine();
                out.println(userInput);
                if ("QUIT".equalsIgnoreCase(userInput)) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
