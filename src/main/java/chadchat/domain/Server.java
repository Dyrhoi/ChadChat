package chadchat.domain;

import chadchat.API.ChadChat;
import chadchat.Ui.Protocol;
import chadchat.domain.message.Message;
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
    /*public User saveUsertoDB(String name ) throws SQLException, ClassNotFoundException {
        User user = new User(-1, name);
       int iD = Database.saveDBUser(name);
       user = Database.getUserfromDB(name);
       user.


        return user;
 } */

    public void broadcast(Client client, Message message){
        for (Client loopedClient : this.clients) {
            if(loopedClient.getUser()==null)
                continue;
            loopedClient.getOutput().println(message);
        }
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
}

