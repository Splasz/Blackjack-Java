package Server;

import Game.Croupier;
import Game.Deck;
import Game.Player;

import java.net.*;
import java.io.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlackjackServer {
    public static List<Player> players = Collections.synchronizedList(new ArrayList<>());
    public static Croupier croupier = new Croupier();
    public static Deck deck = new Deck();
    public static BlackjackProtocol blackjackProtocol = new BlackjackProtocol();

    public static void main(String[] args) throws IOException {
        deck.shuffle();

        try (ServerSocket serverSocket = new ServerSocket(2222)) {
            System.out.println("Serwer uruchomiany...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
