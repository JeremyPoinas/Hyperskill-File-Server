package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {
    private static final int PORT = 1234;
    private static final String ADDRESS = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        System.out.println("Client started!");
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            String messageToSend = "Give me everything you have!";
            output.writeUTF(messageToSend);
            System.out.printf("Sent: %s\n", messageToSend);
            System.out.printf("Received: %s\n", input.readUTF());

        }
    }
}
