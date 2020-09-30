package chadchat.Ui;

import chadchat.domain.Client;
import chadchat.domain.Message;
import chadchat.domain.Server;
import chadchat.domain.User;
import chadchat.infrastructure.Database;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

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
                String commandString = input.substring(1);
                try {
                    Command cmd = fetchCommand(commandString);

                    //Quit was called.
                    if (cmd == null) {
                        break;
                    }

                    cmd.execute(client);
                } catch (ParseException e) {
                    this.client.getOutput().println(e.getMessage());
                }
                continue;
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

    //TODO: Commandlist hashmap implementation

    public Command fetchCommand(String commandString) throws ParseException {
        String[] commandData = commandString.split(" ");
        String commandIdentifier = commandData[0];
        String[] args = commandData.length > 1 ? Arrays.copyOfRange(commandData, 1, commandData.length) : null;
        switch (commandIdentifier) {
            case("quit"):
                return null;
            case("channel"):
                return new ChannelCommand(args);
            case("help"):
                return new HelpCommand();
            default:
                throw new ParseException("Could not find command: " + commandIdentifier, 0);
        }

    }

    public interface Command {
        void execute(Client client);
    }

    public class ChannelCommand implements Command {
        private String subcommand;
        private String[] args;

        public ChannelCommand(String[] args) {
            if(args != null) {
                this.subcommand = args[0];
                this.args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : null;
            }
        }

        @Override
        public void execute(Client client) {
            if(this.subcommand != null) {
                switch (this.subcommand) {
                    case("list"):
                        client.getOutput().println("Here we would list channels:");
                        break;
                    default:
                        client.getOutput().println(getHelperMessage());
                }
            }
            else {
                client.getOutput().println(getHelperMessage());
            }
        }

        public String getHelperMessage() {
            return "Available subcommands for command channel:\n" +
                    "/channel list";
        }
    }

    public class HelpCommand implements Command {

        @Override
        public void execute(Client client) {
            client.getOutput().println("Help command issued: Help");
        }
    }

}
