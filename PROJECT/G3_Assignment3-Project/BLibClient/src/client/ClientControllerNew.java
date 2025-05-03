package client;

import java.io.*;


/**
 * Singleton class that acts as a controller for the client-side operations in the chat system.
 * It manages the lifecycle of the {@link ChatClientNew} instance and provides methods
 * for sending and receiving messages between the client and the server.
 *
 * <p>This class also implements the {@link ChatIF} interface to handle message display.</p>
 *
 */
public class ClientControllerNew implements ChatIF {

    // Class variables *************************************************
    /**
     * Singleton instance of the {@code ClientControllerNew}.
     */
    private static ClientControllerNew instance;

    /**
     * The {@link ChatClientNew} instance that handles client-server communication.
     */
    private ChatClientNew client;

    // Private Constructor (Singleton) *********************************
    /**
     * Private constructor to enforce the Singleton pattern.
     * Creates a new {@link ChatClientNew} instance for managing client-server communication.
     *
     * @param host The server host to connect to.
     * @param port The server port to connect to.
     * @throws IOException If an error occurs while setting up the connection.
     */
    private ClientControllerNew(String host, int port) {
        try {
            client = new ChatClientNew(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't set up connection! Terminating client.");
            System.exit(1);
        }
    }

    // Public method to get the Singleton instance *********************
    /**
     * Returns the Singleton instance of {@code ClientControllerNew}.
     * Initializes the instance if it has not already been created.
     *
     * @param host The server host to connect to.
     * @param port The server port to connect to.
     * @return The Singleton instance of {@code ClientControllerNew}.
     */
    public static ClientControllerNew getInstance(String host, int port) {
        if (instance == null) {
            synchronized (ClientControllerNew.class) {
                if (instance == null) {
                    instance = new ClientControllerNew(host, port);
                }
            }
        }
        return instance;
    }

    /**
     * Returns the Singleton instance of {@code ClientControllerNew}.
     * This method should only be called after the instance has been initialized
     * using {@link #getInstance(String, int)}.
     *
     * @return The Singleton instance of {@code ClientControllerNew}.
     * @throws IllegalStateException If the instance has not been initialized.
     */
    public static ClientControllerNew getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ClientControllerNew is not initialized. Call getInstance(host, port) first.");
        }
        return instance;
    }

    // Getter for ChatClient *******************************************
    /**
     * Returns the {@link ChatClientNew} instance managed by this controller.
     *
     * @return The {@link ChatClientNew} instance.
     */
    public ChatClientNew getChatClient() {
        return client;
    }

    // Instance methods ************************************************
    /**
     * Displays a message in the client console.
     * This method is invoked by the {@link ChatClientNew} to show messages
     * received from the server or generated locally.
     *
     * @param message The message to be displayed.
     */
    @Override
    public void display(String message) {
        System.out.println("> " + message);
    }

    /**
     * Sends a message or command from the client UI to the server.
     *
     * @param str The message or command to be sent to the server.
     */
    public void accept(Object str) {
        client.handleMessageFromClientUI(str);
    }
}

