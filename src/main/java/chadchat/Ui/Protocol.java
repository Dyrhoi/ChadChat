package chadchat.Ui;

import chadchat.API.InvalidPasswordException;
import chadchat.domain.*;
import chadchat.domain.channel.Channel;
import chadchat.domain.channel.ChannelNotFoundException;
import chadchat.domain.message.Message;
import chadchat.domain.user.User;
import chadchat.domain.user.UserExistsException;
import chadchat.domain.user.UserNotFoundException;

import java.text.ParseException;
import java.util.Arrays;

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
        this.client.getOutput().println("Welcome: " + user.getUsername());

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

                    cmd.execute(this);
                } catch (ParseException e) {
                    this.client.getOutput().println(e.getMessage());
                }
                continue;
            }

            Message message = this.server.getChadchat().createMessage(input, this.client.getUser());

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

            try {
                user = this.server.getChadchat().login(username, password);
                System.out.println(client.getIdentifierName() + ": logged into user: " + username);
                break;
            } catch (UserNotFoundException e) {
                this.client.getOutput().println(
                        "This user doesn't exist in our database.\n" +
                                "Do you want to create a new user? yes|no"
                );
                String answer = this.client.getInput().nextLine().strip().toLowerCase();
                if(answer.equals("yes")) {
                    try {
                        user = this.server.getChadchat().createUser(username, password);
                        break;
                    } catch (UserExistsException ex) {
                        this.client.getOutput().println("An error occurred while trying to create the user. Try again.");
                        ex.printStackTrace();
                    }
                }
            } catch (InvalidPasswordException e) {
                this.client.getOutput().println("Invalid password for this user. Try again. Or use a different username.");
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
        void execute(Protocol protocol);
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
        public void execute(Protocol protocol) {
            if(this.subcommand != null) {
                switch (this.subcommand) {
                    case("list"):
                        StringBuilder sb = new StringBuilder();
                        for(Channel c : protocol.server.getChadchat().findAllChannels()) {
                            sb.append(">").append(c).append("\n");
                        }
                        protocol.client.getOutput().println(sb.toString());
                        break;
                    case("create"):
                        if(args != null && args.length == 1) {
                            String name = args[0];
                            protocol.client.getOutput().println("Are you sure you want to create channel: " + name);
                            protocol.client.getOutput().println("Yes | No");
                            if(protocol.client.getInput().nextLine().equalsIgnoreCase("yes")) {
                                try {
                                    protocol.server.getChadchat().createChannel(name, protocol.client.getUser());
                                    protocol.client.getOutput().println("Channel " + name + " was successfully created.");
                                } catch (ChannelNotFoundException e) {
                                    protocol.client.getOutput().println(
                                            "An error occurred while creating the channel.\n" +
                                            "Please try again later."
                                    );
                                    e.printStackTrace();
                                } catch (RuntimeException e) {
                                    protocol.client.getOutput().println(
                                            "Channel with name " + name + " already exists.\n" +
                                            "Use /channel join " + name + " to join this channel."
                                    );

                                }
                            } else {
                                protocol.client.getOutput().println("Aborting channel creation.");
                            }
                        }
                        else {
                            protocol.client.getOutput().println(getHelperMessage());
                        }
                        break;
                    default:
                        protocol.client.getOutput().println(getHelperMessage());
                }
            }
            else {
                protocol.client.getOutput().println(getHelperMessage());
            }
        }

        public String getHelperMessage() {
            return "Available subcommands for command channel:\n" +
                    "/channel list\n" +
                    "/channel create <name>";
        }
    }

    public class HelpCommand implements Command {

        @Override
        public void execute(Protocol protocol) {
            protocol.client.getOutput().println("Help command issued: Help");
        }
    }

}
