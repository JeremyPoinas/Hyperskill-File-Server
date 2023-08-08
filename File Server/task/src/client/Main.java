package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    static final int PORT = 1234;
    static final String ADDRESS = "127.0.0.1";
    enum Action {
        GET, CREATE, DELETE, EXIT
    }
    static Action userAction;
    static String filename;
    static String fileContent;

    public static void main(String[] args) throws IOException {
//        TimeUnit.SECONDS.sleep(10);
//        System.out.println("Client started!");
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            getUserInput();
            switch (userAction) {
                case GET -> output.writeUTF("GET " + filename);
                case CREATE -> output.writeUTF("PUT " + filename + " " + fileContent);
                case DELETE -> output.writeUTF("DELETE " + filename);
                case EXIT -> output.writeUTF("exit ");
            }
            System.out.println("The request was sent.");
            handleAnswer(input);
        }
    }

    private static void handleAnswer(DataInputStream input) throws IOException {
        if (userAction == Action.EXIT) {
            System.exit(0);
            return;
        }
        String userInput = input.readUTF();
        String answerCode = userInput.substring(0, 3);
        String fileContent = userInput.length() > 3 ? userInput.substring(4) : null;
        switch (answerCode) {
            case "200" -> {
                switch (userAction) {
                    case GET -> System.out.printf("The content of the file is: %s\n", fileContent);
                    case CREATE -> System.out.println("The response says that the file was created!");
                    case DELETE -> System.out.println("The response says that the file was successfully deleted!");
                }
            }
            case "404" -> System.out.println("The response says that the file was not found!");
            case "403" -> System.out.println("The response says that creating the file was forbidden!");
        }
    }

    static void getUserInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
        if (scanner.hasNextInt()) {
            userAction = Action.values()[scanner.nextInt() - 1];
            scanner.nextLine();
        } else if (scanner.nextLine().equals("exit")) {
            userAction = Action.EXIT;
            return;
        }

        System.out.println("Enter filename:");
        filename = scanner.nextLine();

        if (userAction == Action.CREATE) {
            System.out.println("Enter file content:");
            fileContent = scanner.nextLine();
        }
    }
}
