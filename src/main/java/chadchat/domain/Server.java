package chadchat.domain;

import chadchat.API.ChadChat;
import chadchat.Ui.Protocol;
import chadchat.domain.channel.ChannelNotFoundException;
import chadchat.domain.message.Message;
import chadchat.domain.user.User;
import chadchat.infrastructure.Database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Server {
    private final int port;
    private boolean isRunning;
    private List<Client> clients;
    private final ChadChat chadchat;


    public Server(int port) {
        Database database = new Database();
        this.chadchat = new ChadChat(database, database, database);
        this.port = port;

        this.isRunning = true;
        this.clients = new ArrayList<>();
    }

    public void broadcast(Client client, Message message) throws ChannelNotFoundException {
        for (Client loopedClient : this.clients) {
            //If user is not initialized or the user isn't subscribed to the message channel, ignore.
            if (loopedClient.getUser() == null ||
                !chadchat.channelContainsUser(loopedClient.getUser(), message.getChannel().getId())
            )
                continue;
            //Print if conditions are false.
            loopedClient.getOutput().println(message);
        }
        System.out.println(message);
    }

    public void start() throws IOException {
        System.out.println(" Starting server...");

        ServerSocket serverSocket = new ServerSocket(this.port);
        System.out.println("Server started on port: " + this.port);

        while (this.isRunning) {
            Socket socket = serverSocket.accept();

            Client client = new Client(socket);
            Protocol protocol = new Protocol(client, this);

            clients.add(client);
            System.out.println("Socket connected: " + client.getIdentifierName());

            new Thread(() -> {

                try {
                    protocol.run();

                } catch (NoSuchElementException e) {
                    System.out.println("Socket's InputStream died. This usually is the result of a force exit.");
                    e.printStackTrace();
                } finally {
                    try {
                        System.out.println("Socket disconnected: " + client.getIdentifierName());
                        clients.remove(client);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();


        }
    }

    public ChadChat getChadchat() {
        return chadchat;
    }

    public List<String> getOnlineUsers() {
        List<String> users = new ArrayList<>();
        for(Client client : clients) {
            //Make sure user is initialized...
            if(client.getUser() != null) users.add(client.getUser().getUsername());
        }
        return users;
    }
}


