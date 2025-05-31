package Server;

import Game.Croupier;
import Game.Deck;
import Game.Player;

import java.net.*;
import java.io.*;

import java.util.*;

public class BlackjackServer {
    public static List<Player> players = new ArrayList<>();
    public static List<ClientHandler> clientHandlers = new ArrayList<>();
    public static final BlackjackProtocol blackjackProtocol = new BlackjackProtocol();

    public static boolean gameStarted = false;

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(2222)) {
            System.out.println("Serwer uruchomiany...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowy klient połączony");
                new Thread(new ClientHandler(clientSocket)).start();
                System.out.println("Lista graczy: " + players);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
