package server;

import java.io.DataInputStream;
import java.io.IOException;

class Request {
    enum Action {
        PUT, GET, DELETE, EXIT
    }
    Action action;

    enum SearchMethod {
        BY_NAME, BY_ID
    }
    SearchMethod searchMethod;

    byte[] content;

    String filename;

    String fileId;

    public Request(DataInputStream input) throws IOException {
        String[] request = input.readUTF().split(" ");
        this.action = Action.valueOf(request[0]);

        switch (action) {
            case GET, DELETE -> {
                this.searchMethod = SearchMethod.valueOf(request[1]);
                if (searchMethod == SearchMethod.BY_ID) {
                    this.fileId = request[2];
                } else {
                    this.filename = request[2];
                }
            }
            case PUT -> {
                this.filename = request[1];
                int contentLength = input.readInt();
                this.content = new byte[contentLength];
                input.read(content);
            }
        }
    }
}
