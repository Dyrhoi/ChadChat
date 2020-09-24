package chadchat.Ui;

import chadchat.domain.Client;
import chadchat.domain.Message;
import chadchat.domain.Server;
import chadchat.domain.User;

import java.io.IOException;

public class Protocol implements Runnable {

    private final Client client;
    private final Server server;

    public Protocol(Client client, Server server){
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        /**
         *
         *
         * Alt client logik...
         *
         * *
         */

        client.getOutput().println("\n \uD83D\uDCBB " + " ... Connected to Chat Room - Welcome .... " + "\uD83D\uDCBB");

        String line;
        while(!(line = client.getInput().nextLine()).equalsIgnoreCase("quit")) {
            if(line.startsWith("/user")){
                client.getOutput().println("Skriv dit brugernavn: ");
                String userName = client.getInput().nextLine();
                this.client.setUser(new User(-1, userName));
                continue;
            }
            Message message = new Message(line,this.client.getUser());
            System.out.println(client.getIdentifierName() + " issued: " + line);

            this.server.broadcast(this.client, message);
        }
    }
}
