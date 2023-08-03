package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileServer {
    ArrayList<String> fileServer = new ArrayList<>();

    List<String> authorizedFiles = Arrays.asList("file1", "file2", "file3", "file4", "file5", "file6", "file7", "file8", "file9", "file10");

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
