package chadchat.domain;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Server {
    private final int port;
    private boolean isRunning;
    private List<Client> clients;

    public Server(int port) {
        this.port = port;

        this.isRunning = true;
        this.clients = new ArrayList<>();
    }

    public void start() throws IOException {
        System.out.println("Starting server...");

        ServerSocket serverSocket = new ServerSocket(this.port);
        System.out.println("Server started on port: " + this.port);

        while(this.isRunning) {
            Socket socket = serverSocket.accept();

            Client client = new Client(socket);

            clients.add(client);

            System.out.println("Socket connected: " + client.getIdentifierName());

            new Thread(() -> {

                try {
                    /**
                     *
                     *
                     * Alt client logik...
                     *
                     * *
                     */

                    client.getOutput().println("Connected to Chat Room.");

                    String line;
                    while(!(line = client.getInput().nextLine()).equalsIgnoreCase("quit")) {

                        System.out.println(client.getIdentifierName() + " issued: " + line);

                        for(Client loopedClient : this.clients) {
                            String name = (loopedClient == client) ? "You" : loopedClient.getIdentifierName();

                            loopedClient.getOutput().println(name + ": " + line);
                        }
                    }

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
}
