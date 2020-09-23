package chadchat.entries;

import chadchat.domain.Server;

import java.io.IOException;

public class ServerEntry {

    public static void main(String[] args) {
        int port = 2222;
        Server server = new Server(port);
        try {
            server.start();
        } catch (IOException e) {
            System.out.println("Failure in setting up Server Socket.");
            e.printStackTrace();
        }
    }

}
