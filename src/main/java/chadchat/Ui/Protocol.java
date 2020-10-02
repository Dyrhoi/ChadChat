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
import java.util.List;
import java.util.NoSuchElementException;

public class Protocol implements Runnable {

    private final Client client;
    private final Server server;

    public Protocol(Client client, Server server) {
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

        this.client.getOutput().println("Connected to Chat Room - Welcome!  :) <3  ");

        User user = initUser();
        this.client.setUser(user);
        this.client.getOutput().println("Welcome: " + user.getUsername());
        this.client.getOutput().println("Type /help for help");
        joinNextChannel();

        String input;
        while (true) {
            input = client.getInput().nextLine();

            if (input.startsWith("/")) {
                System.out.println(client.getIdentifierName() + " issued command: " + input);
                //Executing command:
                String commandString = input.substring(1);
                try {
                    Command cmd = fetchCommand(commandString);

                    //Quit was called.
                    //We can no longer kill the socket, message client will bug. @Christian
                    //Just destroy user.
                    /*if (cmd == null) {
                        client.setUser(null);
                        client.getOutput().println("You've been signed out. You can close the chat window.");
                        break;
                    }*/

                    cmd.execute(this);
                } catch (ParseException e) {
                    this.client.getOutput().println(e.getMessage());
                }
                continue;
            }

            if(this.client.getUser().getCurrentChannel() == -1) {
                this.client.getOutput().println("You're currently not in any channels, please join a channel.");
                continue;
            }
            Message message = this.server.getChadchat().createMessage(input, this.client.getUser());
            try {
                this.server.broadcast(this.client, message);
            } catch (ChannelNotFoundException e) {
                //Very unlikely error, trying to send a message to a channel that doesn't exist.
                client.getOutput().println("An internal error occurred. Try again.");
                e.printStackTrace();
            }
        }
    }

    public User initUser() {
        User user;


        String[] input;
        do {
            this.client.getOutput().println("Enter your (current or new) username and password.\n" +
                    "Type 'username password'");
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
                if (answer.equals("yes")) {
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

        } while (true);

        return user;
    }

    private void joinNextChannel() {
        Iterable<Channel> joinedChannels = this.server.getChadchat().findAllChannelsByUser(this.client.getUser());
        try {
            Channel nextChannel = joinedChannels.iterator().next();
            this.client.getUser().setCurrentChannel(nextChannel.getId());
            this.client.getOutput().println("You're now writing in channel: " + nextChannel.getName());
        } catch (NoSuchElementException e) {
            this.client.getOutput().println("You're not in any channels, use /channel to get started.");
        }

    }


    /**
     * @Class Command : Our command executor.
     */

    //TODO: Commandlist hashmap implementation
    public Command fetchCommand(String commandString) throws ParseException {
        String[] commandData = commandString.split(" ");
        String commandIdentifier = commandData[0];
        String[] args = commandData.length > 1 ? Arrays.copyOfRange(commandData, 1, commandData.length) : null;
        switch (commandIdentifier) {
            /*case ("quit"):
                return null;*/
            case ("channel"):
                return new ChannelCommand(args);
            case ("help"):
                return new HelpCommand();
            case ("online"):
                return new OnlineCommand();
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
            if (args != null) {
                this.subcommand = args[0];
                this.args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : null;
            }
        }

        @Override
        public void execute(Protocol protocol) {
            if (this.subcommand != null) {
                switch (this.subcommand) {
                    case ("list"):
                        list(protocol);
                        break;
                    case ("create"):
                        create(protocol);
                        break;
                    case ("join"):
                        join(protocol);
                        break;
                    case ("leave"):
                        leave(protocol);
                        break;
                    case ("set"):
                        set(protocol);
                        break;
                    case ("subscribed"):
                        subscribed(protocol);
                        break;
                    default:
                        protocol.client.getOutput().println(getHelperMessage());
                }
            } else {
                protocol.client.getOutput().println(getHelperMessage());
            }
        }

        public void list(Protocol protocol) {
            StringBuilder sb = new StringBuilder();
            for (Channel c : protocol.server.getChadchat().findAllChannels()) {
                sb.append(">").append(c).append("\n");
            }
            protocol.client.getOutput().println(sb.toString());
        }

        public void create(Protocol protocol) {
            if (args != null && args.length == 1) {
                String name = args[0];
                protocol.client.getOutput().println("Are you sure you want to create channel: " + name);
                protocol.client.getOutput().println("Yes | No");
                if (protocol.client.getInput().nextLine().equalsIgnoreCase("yes")) {
                    try {
                        protocol.server.getChadchat().createChannel(name, protocol.client.getUser());
                        protocol.client.getOutput().println("Channel " + name + " was successfully created.");
                        set(protocol);
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
            } else {
                protocol.client.getOutput().println(getHelperMessage());
            }
        }

        public void join(Protocol protocol) {
            if (args != null && args.length == 1) {
                String name = args[0];
                try {
                    protocol.server.getChadchat().joinChannel(
                            name,
                            protocol.client.getUser()
                    );
                    protocol.client.getOutput().println("You successfully joined the channel: " + name);
                    set(protocol);
                } catch (ChannelNotFoundException e) {
                    protocol.client.getOutput().println("The channel " + name + " does not exist\n"
                            + "Use '/channel list' for all available channels");
                    e.printStackTrace();
                } catch (UserExistsException e) {
                    protocol.client.getOutput().println("You're already subscired to channel " + name);
                    e.printStackTrace();
                }
            }
        }

        public void leave(Protocol protocol) {
            if (args != null && args.length == 1) {
                String name = args[0];
                try {
                    protocol.server.getChadchat().leaveChannel(name, protocol.client.getUser());
                    if(protocol.server.getChadchat().findChannel(name).getName().equalsIgnoreCase(name)) {
                        protocol.client.getOutput().println("You've left channel: " + name);
                        joinNextChannel();
                    }
                } catch (ChannelNotFoundException e) {
                    protocol.client.getOutput().println("Channel " + name + " not found. Try again.");
                    e.printStackTrace();
                } catch (UserExistsException e) {
                    protocol.client.getOutput().println("You're not in channel " + name + ". Try again.");
                    e.printStackTrace();
                }
            }
        }

        public void subscribed(Protocol protocol) {
            protocol.client.getOutput().println(
               "You're part of channels:\n" +
                protocol.server.getChadchat().findAllChannelsByUser(protocol.client.getUser())
            );

        }

        public void set(Protocol protocol) {
            System.out.println("test");
            if (args != null && args.length == 1) {
                String name = args[0];
                try {
                    int channelId = protocol.server.getChadchat().findChannel(name).getId();
                    if(protocol.server.getChadchat().channelContainsUser(protocol.client.getUser(), channelId)) {
                        protocol.client.getUser().setCurrentChannel(channelId);
                        protocol.client.getOutput().println("You're now writing in channel: " + name);
                    }
                    else {
                        protocol.client.getOutput().println("You can't write in a channel you're not part of.");
                    }

                } catch (ChannelNotFoundException e) {
                    protocol.client.getOutput().println("This channel does not exist.");
                    e.printStackTrace();
                }
            }
        }

        public String getHelperMessage() {
            return "Available subcommands for command: 'channel':\n" +
                    "/channel list : List all available channels.\n" +
                    "/channel create <name> : Create a channel.\n" +
                    "/channel join <name> : Join a channel.\n" +
                    "/channel leave <name> : Leave a channel.\n" +
                    "/channel subscribed : List of all channels you're subscribed to.\n" +
                    "/channel set <name> : Set the channel your messages will appear in.";
        }
    }

    public class HelpCommand implements Command {

        @Override
        public void execute(Protocol protocol) {
            String channelHelp = new ChannelCommand(new String[] {"this needs", "to be here"}).getHelperMessage();
            protocol.client.getOutput().println("" +
                    "List of available commands:\n" +
                    channelHelp + "\n" +
                    "/online : Display online users\n" /*+
                    "/quit : Log out of the chat."*/
            );
        }

    }

    public class OnlineCommand implements Command {

        @Override
        public void execute(Protocol protocol) {

            client.getOutput().println(protocol.server.getOnlineUsers().toString());

        }

    }
}
