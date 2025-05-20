package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler extends Thread {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Witaj w blackjacku!");
            String input;

            while ((input = in.readLine()) != null) {
                System.out.println("Klient "+ Thread.currentThread().getName() +  " mówi: " + input);
                if (input.equalsIgnoreCase("exit")) {
                    out.println("Do widzenia!");
                    break;
                } else {
                    out.println("Komenda nierozpoznana.");
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

