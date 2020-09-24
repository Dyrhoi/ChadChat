package chadchat.domain;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final PrintWriter output;
    private final Scanner input;

    private final String identifierName;
    private User user;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new Scanner(socket.getInputStream());
        this.identifierName = socket.getInetAddress().getHostAddress() + " : " + socket.getPort();
    }

    public PrintWriter getOutput() {
        return output;
    }

    public Scanner getInput() {
        return input;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
