package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    static final int PORT = 1234;
    static final String ADDRESS = "127.0.0.1";
    enum Action {
        GET, CREATE, DELETE, EXIT
    }

    enum SearchMethod {
        NAME, ID
    }
    static Action userAction;
    static SearchMethod searchMethod;
    static String filename;
    static String fileId;
    static String filenameServer;
    static final String FILEPATH =  System.getProperty("user.dir") + "/src/client/data/";

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(1000);
        try (Socket socket = new Socket(ADDRESS, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            getUserAction();
            switch (userAction) {
                case GET -> {
                    handleGetAction(output);
                    handleGetAnswer(input);
                }
                case CREATE -> {
                    handleCreateAction(output);
                    handleCreateAnswer(input);
                }
                case DELETE -> {
                    handleDeleteAction(output);
                    handleDeleteAnswer(input);
                }
                case EXIT -> handleExitAction(output);
            }
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    static void handleGetAnswer(DataInputStream input) throws IOException {
        String answerCode = input.readUTF();
        switch (answerCode) {
            case "200" -> {
                int fileLength = input.readInt();
                byte[] content = new byte[fileLength];
                input.read(content);

                Scanner scanner = new Scanner(System.in);
                System.out.println("The file was downloaded! Specify a name for it:");
                String newFileName = scanner.nextLine();

                File file = new File(FILEPATH + newFileName);
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(content);
                }
                System.out.println("File saved on the hard drive!");
            }
            case "404" -> System.out.println("The response says that this file is not found!");
        }

    }

    static void handleCreateAnswer(DataInputStream input) throws IOException {
        String userInput = input.readUTF();
        String answerCode = userInput.substring(0, 3);
        String fileId = userInput.length() > 3 ? userInput.substring(4) : null;

        switch (answerCode) {
            case "200" -> System.out.printf("Response says that file is saved! ID = %s, where %s is an id of the file!\n", fileId, fileId);
            case "403" -> System.out.println("The response says that creating the file was forbidden!");
        }
    }

    static void handleDeleteAnswer(DataInputStream input) throws IOException {
        String userInput = input.readUTF();
        String answerCode = userInput.substring(0, 3);

        switch (answerCode) {
            case "200" -> System.out.println("The response says that the file was successfully deleted!");
            case "403" -> System.out.println("The response says that the file was not found!");
        }
    }

    static void getUserAction() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter action (1 - get a file, 2 - create a file, 3 - delete a file):");
        if (scanner.hasNextInt()) {
            userAction = Action.values()[scanner.nextInt() - 1];
            scanner.nextLine();
        } else if (scanner.nextLine().equals("exit")) {
            userAction = Action.EXIT;
        }
    }

    static void handleGetAction(DataOutputStream output) throws IOException {
        getSearchParameters();
        switch (searchMethod) {
            case NAME -> output.writeUTF("GET BY_NAME " + filename);
            case ID -> output.writeUTF("GET BY_ID " + fileId);
        }
        System.out.println("The request was sent.");
    }

    static void handleDeleteAction(DataOutputStream output) throws IOException {
        getSearchParameters();
        switch (searchMethod) {
            case NAME -> output.writeUTF("DELETE BY_NAME " + filename);
            case ID -> output.writeUTF("DELETE BY_ID " + fileId);
        }
        System.out.println("The request was sent.");
    }

    static void handleCreateAction(DataOutputStream output) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter name of the file:");
        filename = scanner.nextLine();
        while (filename.isEmpty()) {
            filename = scanner.nextLine();
        }
        File file = new File(FILEPATH + filename);
        if (!file.isFile()) {
            throw new IOException("The file does not exist in the data folder.");
        }
        byte[] message;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            long fileLength = file.length();
            message = new byte[(int) fileLength];
            inputStream.read(message);
        }
        System.out.println("Enter name of the file to be saved on server:");
        filenameServer = scanner.nextLine();
        if (filenameServer.isEmpty()) {
            filenameServer = filename;
        }

        output.writeUTF("PUT " + filenameServer);
        output.writeInt(message.length);
        output.write(message);
        System.out.println("The request was sent.");
    }

    static void getSearchParameters() {
        System.out.printf("Do you want to %s the file by name or by id (1 - name, 2 - id):", userAction == Action.GET ? "get" : "delete");
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextInt()) {
            searchMethod = SearchMethod.values()[scanner.nextInt() - 1];
            scanner.nextLine();
        }
        switch (searchMethod) {
            case NAME -> {
                System.out.println("Enter filename:");
                filename = scanner.nextLine();
            }
            case ID -> {
                System.out.println("Enter id:");
                fileId = scanner.nextLine();
            }
        }
    }

    private static void handleExitAction(DataOutputStream output) throws IOException {
        output.writeUTF("EXIT ");
        System.out.println("The request was sent.");
        System.exit(0);
    }
}
