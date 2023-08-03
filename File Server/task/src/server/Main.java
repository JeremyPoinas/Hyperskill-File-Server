package server;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        FileServer fileServer = new FileServer();
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
        }
    }
}