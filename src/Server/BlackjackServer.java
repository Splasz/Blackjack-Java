package Server;

import Game.Player;
import java.net.*;
import java.util.*;

public class BlackjackServer {
    public static List<Player> players = new ArrayList<>();
    public static List<ClientHandler> clientHandlers = new ArrayList<>();
    public static final BlackjackProtocol blackjackProtocol = new BlackjackProtocol();
    public static int finishedPlayers = 0;

    public static boolean gameStarted = false;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(2222)) {
            System.out.println("Serwer uruchomiony...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowy klient połączony");
                new Thread(new ClientHandler(clientSocket)).start();

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
