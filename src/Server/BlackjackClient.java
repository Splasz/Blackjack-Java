package Server;

import java.io.*;
import java.net.*;

public class BlackjackClient {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String fromServer, fromUser;

                while ((fromServer = in.readLine()) != null) {
                    System.out.println("Serwer: " + fromServer);
                    if (fromServer.equals("Bye.")) break;

                    System.out.print("Ty: ");
                    fromUser = stdIn.readLine();

                    if (fromUser != null) {
                        out.println(fromUser);
                    }
                }

            }

        }
    }
