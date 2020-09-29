package chadchat.Ui;

import chadchat.domain.Client;
import chadchat.domain.Message;
import chadchat.domain.Server;
import chadchat.domain.User;
import chadchat.infrastructure.Database;

import java.sql.SQLException;
import java.text.ParseException;

public class Protocol implements Runnable {

    private final Client client;
    private final Server server;

    public Protocol(Client client, Server server){
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {

        /*
        *
        * Retrieve user or create user.
        *
        * */

        this.client.getOutput().println("\n \uD83D\uDCBB " + " ... Connected to Chat Room - Welcome .... " + "\uD83D\uDCBB");

        User user = initUser();
        this.client.setUser(user);
        this.client.getOutput().println("Welcome: " + user.getName());

        String input;
        while(true) {
            input = client.getInput().nextLine();
            if(input.startsWith("/")) {
                System.out.println(client.getIdentifierName() + " issued command: " + input);
                //Executing command:
                String[] commandArray = input.split(" ");
                String commandString = commandArray[0].substring(1);
                try {
                    Command cmd = fetchCommand(commandString);

                    //Quit was called.
                    if (cmd == null) {
                        break;
                    }

                    cmd.execute(client);
                    continue;
                } catch (ParseException e) {
                    this.client.getOutput().println(e.getMessage());
                    continue;
                }
            }

            Message message = new Message(input,this.client.getUser());
            System.out.println(client.getIdentifierName() + " : " + input);

            this.server.broadcast(this.client, message);
        }
    }

    public User initUser() {
        User user;

        String[] input;
        do {
            this.client.getOutput().println("Enter your (current or new) username and password.");
            input = this.client.getInput().nextLine().split(" ");
            if (input.length != 2) {
                this.client.getOutput().println("Incorrect format, correct format is: username password");
                continue;
            }
            String username = input[0];
            String password = input[1];

            //find user in database
            if((user = this.server.fetchUser(username)) != null) {
                //Try to login...
                break;
            }
            this.client.getOutput().println(
                    "This user doesn't exist in our database.\n" +
                    "Do you want to create a new user? yes|no"
            );
            String answer = this.client.getInput().nextLine().strip().toLowerCase();
            if(answer.equals("yes")) {
                user = this.server.createUser(username, password);
                break;
            }

        } while(true);

        return user;
    }


    /**
     *
     *
     * @Class Command : Our command executor.
     *
     *
     * */

    public Command fetchCommand(String commandString) throws ParseException {
        switch (commandString) {
            case("quit"):
                return null;
            case("channels"):
                return new ListChannelsCommand();
        }
        throw new ParseException("Could not find command: " + commandString, 0);
    }

    public interface Command {
        void execute(Client client);
    }

    public class ListChannelsCommand implements Command {

        @Override
        public void execute(Client client) {
            client.getOutput().println("We're now listing the channels:");
        }
    }

}
