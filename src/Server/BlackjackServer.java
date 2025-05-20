package Server;

import Game.Blackjack;

import java.net.*;
import java.io.*;

import java.net.*;
import java.io.*;

public class BlackjackServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serwer uruchomiony...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Nowy klient: " + clientSocket.getInetAddress());
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}
