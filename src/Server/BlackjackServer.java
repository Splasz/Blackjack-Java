package Server;

import java.net.*;
import java.io.*;

public class BlackjackServer {
    public static void main(String[] args) throws IOException {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            BlackjackProtocol blackjackProtocol = new BlackjackProtocol();
            String inputLinem, outputLinem;

            outputLinem = blackjackProtocol.processInput(null);
            out.println(outputLinem);

            while ((inputLinem = in.readLine()) != null) {
                outputLinem = blackjackProtocol.processInput(inputLinem);
                out.println(outputLinem);
                if (outputLinem.equals("Bye.")) break;
            }

            clientSocket.close();
        }
    }
}
