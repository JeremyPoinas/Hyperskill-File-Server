package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    static final int PORT = 1234;
    static final String FILEPATH =  System.getProperty("user.dir") + "/src/server/data/";
    static ArrayList<String> fileList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
//        System.out.println("Server started!");
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try (Socket socket = server.accept();
                     DataInputStream input = new DataInputStream(socket.getInputStream());
                     DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                    String[] userInput = input.readUTF().split(" ");
                    String command = userInput[0];
                    String fileName = userInput.length > 1 ? userInput[1] : null;
                    String fileContent = userInput.length > 2 ? String.join(" ", Arrays.copyOfRange(userInput, 2, userInput.length)) : null;

                    switch (command) {
                        case "PUT" -> add(fileName, fileContent, output);
                        case "GET" -> get(fileName, output);
                        case "DELETE" -> delete(fileName, output);
                        case "exit" -> System.exit(0);
                        default -> System.out.println("Command unknown");
                    }
                }
            }
        }
    }

    static void add(String fileName, String fileContent, DataOutputStream output) throws IOException {
        if (fileList.contains(fileName)) {
            output.writeUTF("403");
        } else {
            File newFile = new File(FILEPATH + fileName);
            try (FileWriter writer = new FileWriter(newFile)) {
                writer.write(fileContent);
                newFile.delete();
                fileList.add(fileName);
                output.writeUTF("200");
            }
        }
    }

    static void delete(String fileName, DataOutputStream output) throws IOException {
        if (!fileList.contains(fileName)) {
            output.writeUTF("404");
        } else {
            File fileToDelete = new File(FILEPATH + fileName);
            fileToDelete.delete();
            fileList.remove(fileName);
            output.writeUTF("200");
        }
    }

    static void get(String fileName, DataOutputStream output) throws IOException {
        if (!fileList.contains(fileName)) {
            output.writeUTF("404");
        } else {
            String fileToReadPath = FILEPATH + fileName;
            try {
                String fileContent = readFileAsString(fileToReadPath);
                fileList.add(fileName);
                output.writeUTF("200" + " " + fileContent);
            } catch (IOException e) {
                System.out.println("Cannot read file: " + e.getMessage());
            }
        }
    }

    static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }
}