package chadchat.domain;

import chadchat.domain.user.User;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final PrintWriter output;
    private final Scanner input;

    private final String identifierName;
    private User user;

    public Client(Socket socket) throws IOException {
        this.socket = socket;

        this.output = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true);
        this.input = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8);
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
