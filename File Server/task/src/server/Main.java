package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Main implements Serializable {
    static final int PORT = 1234;
    static final String FILEPATH =  System.getProperty("user.dir") + "/src/server/data/";
    static ExecutorService executor = Executors.newFixedThreadPool(4);
    static class FileList extends HashMap<String, String> implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    static FileList fileList = new FileList();

    public static void main(String[] args) throws IOException {
        deserializeFileList();
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                executor.submit(() -> {
                    try (DataInputStream input = new DataInputStream(socket.getInputStream());
                         DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                    ) {
                        Request request = new Request(input);

                        switch (request.action) {
                            case PUT -> handlePutRequest(request, output);
                            case GET -> handleGetRequest(request, output);
                            case DELETE -> handleDeleteRequest(request, output);
                            case EXIT -> {
                                SerializationUtils.serialize(fileList, FILEPATH + "fileList.data");
                                server.close();
                                System.exit(0);
                            }
                            default -> System.out.println("Command unknown");
                        }
                        socket.close();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            SerializationUtils.serialize(fileList, FILEPATH + "fileList.data");
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    static void deserializeFileList() {
        File file = new File(FILEPATH + "fileList.data");
        if (file.exists() && !file.isDirectory()) {
            try {
                fileList = (FileList) SerializationUtils.deserialize(FILEPATH + "fileList.data");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static synchronized void handlePutRequest(Request request, DataOutputStream output) throws IOException {
        String fileId = generateFileId(request.filename);
        if (fileList.containsValue(request.filename)) {
            request.filename = request.filename + fileId;
        }
        File newFile = new File(FILEPATH + request.filename);
        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            outputStream.write(request.content);
            fileList.put(fileId, request.filename);
            output.writeUTF("200" + " " + fileId);
        } catch (IOException e) {
            output.writeUTF("403");
        }
    }

    private static String generateFileId(String filename) {
        Random random = new Random();
        String fileId;
        do {
            fileId = random.nextInt(10) + "";
        } while (fileList.containsKey(fileId) || fileList.containsValue(filename + fileId));
        return fileId;
    }

    static synchronized void handleDeleteRequest(Request request, DataOutputStream output) throws IOException {
        switch (request.searchMethod) {
            case BY_ID -> {
                if (!fileList.containsKey(request.fileId)) {
                    output.writeUTF("404");
                    return;
                }
                request.filename = fileList.get(request.fileId);
            }
            case BY_NAME -> {
                if (!fileList.containsValue(request.filename)) {
                    output.writeUTF("404");
                    return;
                }
            }
        }
        File fileToDelete = new File(FILEPATH + request.filename);
        fileToDelete.delete();
        if (request.searchMethod == Request.SearchMethod.BY_ID) {
            fileList.remove(request.fileId);
        } else {
            fileList.values().remove(request.filename);
        }
        output.writeUTF("200");
    }

    static synchronized void handleGetRequest(Request request, DataOutputStream output) throws IOException {
        switch (request.searchMethod) {
            case BY_ID -> {
                if (!fileList.containsKey(request.fileId)) {
                    output.writeUTF("404");
                    return;
                }
                request.filename = fileList.get(request.fileId);
            }
            case BY_NAME -> {
                if (!fileList.containsValue(request.filename)) {
                    output.writeUTF("404");
                    return;
                }
            }
        }

        File file = new File(FILEPATH + request.filename);
        byte[] message;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            long fileLength = file.length();
            message = new byte[(int) fileLength];
            inputStream.read(message);
            output.writeUTF("200");
            output.writeInt(message.length);
            output.write(message);
        } catch (IOException e) {
            System.out.println("Cannot read file: " + e.getMessage());
        }
    }
}