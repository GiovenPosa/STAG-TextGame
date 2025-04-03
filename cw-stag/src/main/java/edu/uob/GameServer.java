package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    private final GameController gameController;

    public static void main(String[] args) throws IOException {
        StringBuilder entityFilePath;
        entityFilePath = new StringBuilder();
        entityFilePath.append("config").append(File.separator).append("extended-entities.dot");

        StringBuilder actionFilePath;
        actionFilePath = new StringBuilder();
        actionFilePath.append("config").append(File.separator).append("extended-actions.xml");

        File entitiesFile = Paths.get(entityFilePath.toString()).toAbsolutePath().toFile();
        File actionsFile = Paths.get(actionFilePath.toString()).toAbsolutePath().toFile();
        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Instanciates a new server instance, specifying a game with some configuration files
    *
    * @param entitiesFile The game configuration file containing all game entities to use in your game
    * @param actionsFile The game configuration file containing all game actions to use in your game
    */
    public GameServer(File entitiesFile, File actionsFile) {
        LoadEntityFile loadEntityFile = new LoadEntityFile(entitiesFile);
        LoadActionFile loadActionFile = new LoadActionFile(actionsFile);
        gameController = new GameController(loadEntityFile, loadActionFile);
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * This method handles all incoming game commands and carries out the corresponding actions.</p>
    *
    * @param command The incoming command to be processed
    */
    public String handleCommand(String command) {
        return gameController.getCommandFromServer(command);
    }

    private static String stringBuilder(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Starts a *blocking* socket server listening for new connections.
    *
    * @param portNumber The port to listen on.
    * @throws IOException If any IO related operation fails.
    */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.print("Server listening on port ");
            System.out.println(portNumber);
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
    * Do not change the following method signature or we won't be able to mark your submission
    * Handles an incoming connection from the socket server.
    *
    * @param serverSocket The client socket to read/write from.
    * @throws IOException If any IO related operation fails.
    */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                System.out.println(stringBuilder("Received message from ", incomingCommand));
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                writer.newLine();
                writer.write(END_OF_TRANSMISSION);
                writer.newLine();
                writer.flush();
            }
        }
    }
}
