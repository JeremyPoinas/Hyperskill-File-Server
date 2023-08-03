package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final int PORT = 1234;
    ArrayList<String> fileServer = new ArrayList<>();

    List<String> authorizedFiles = Arrays.asList("file1", "file2", "file3", "file4", "file5", "file6", "file7", "file8", "file9", "file10");

    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");
        try (ServerSocket server = new ServerSocket(PORT)) {
            try (Socket socket = server.accept();
                 DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())
            ) {
                System.out.printf("Received: %s\n", input.readUTF());
                String messageToSend = "All files were sent!";
                output.writeUTF(messageToSend);
                System.out.printf("Sent: %s\n", messageToSend);
            }
        }

        /*Main fileServer = new Main();
        Scanner scanner = new Scanner(System.in);
        String[] input = scanner.nextLine().split(" ");
        String command = input[0];
        String fileName = input.length > 1 ? input[1] : null;

        while (true) {
            switch (command) {
                case "add" -> fileServer.add(fileName);
                case "get" -> fileServer.get(fileName);
                case "delete" -> fileServer.delete(fileName);
                case "exit" -> System.exit(0);
                default -> System.out.println("Command unknown");
            }
            input = scanner.nextLine().split(" ");
            command = input[0];
            fileName = input.length > 1 ? input[1] : null;
        }*/
    }

    void add(String fileName) {
        if (!authorizedFiles.contains(fileName) || fileServer.contains(fileName) || fileServer.size() == 10) {
            System.out.printf("Cannot add the file %s\n", fileName);
        } else {
            fileServer.add(fileName);
            System.out.printf("The file %s added successfully\n", fileName);
        }
    }

    void delete(String fileName) {
        if (!fileServer.contains(fileName)) {
            System.out.printf("The file %s not found\n", fileName);
        } else {
            fileServer.remove(fileName);
            System.out.printf("The file %s was deleted\n", fileName);
        }
    }

    void get(String fileName) {
        if (!fileServer.contains(fileName)) {
            System.out.printf("The file %s not found\n", fileName);
        } else {
            System.out.printf("The file %s was sent\n", fileName);
        }
    }
}