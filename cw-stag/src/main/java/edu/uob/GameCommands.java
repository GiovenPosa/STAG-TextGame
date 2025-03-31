package edu.uob;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameCommands {
    private final GameController gameController;
    private final GameCustomCommands gameCustomCommands;
    private GameLocations gameLocations;
    private String currentPlayerName;
    private String currentPlayerLocation;

    public GameCommands(GameController gameController) {
        this.gameController =  gameController;
        gameCustomCommands = new GameCustomCommands(gameController);
    }

    public String checkCommand(String command) {
        Pattern commandPattern = Pattern.compile("^([^:]+):(.*)$");
        Matcher commandMatcher = commandPattern.matcher(command);
        if (!commandMatcher.find()) {
            return "Invalid command.\n";
        }
        String playerName = commandMatcher.group(1).trim().toLowerCase();
        String playerCommand = commandMatcher.group(2).trim().toLowerCase();

        GamePlayers currentPlayer = this.gameController.getOrCreatePlayer(playerName);
        this.currentPlayerName = currentPlayer.getPlayerName();
        this.currentPlayerLocation = this.gameController.getCurrentLocations(this.currentPlayerName);

        LinkedList<String> commandParts = new LinkedList<>(Arrays.asList(playerCommand.split(" ")));
        if (commandParts.isEmpty()) {
            return "Invalid command.\n";
        }
        String mainCommand = commandParts.get(0);

        if ("look".equals(mainCommand)) {
            if (commandParts.size() > 1) {
                return "Invalid Look Command.\n";
            }
            return this.lookCommand();
        }
        else if ("goto".equals(mainCommand)) {
            if (commandParts.size() != 2) {
                return "Invalid Goto Command: Choose A Location.\n";
            }
            return this.goToCommand(playerCommand);
        }
        else if ("inventory".equals(mainCommand) || "inv".equals(mainCommand)) {
            if (commandParts.size() > 1) {
                return "Invalid Inventory Command.\n";
            }
            return this.inventoryCommand();
        }
        else if ("get".equals(mainCommand)) {
            if (commandParts.size() != 2) {
                return "Invalid Get Command: Choose An Artefact To Pick Up.\n";
            }
            return this.getCommand(playerCommand);
        }
        else if ("drop".equals(mainCommand)) {
            if (commandParts.size() != 2) {
                return "Invalid Drop Command: Choose An Artefact To Drop.\n";
            }
            return this.dropCommand(playerCommand);
        }
        else {
            return this.gameCustomCommands.getCustomCommands(playerName, playerCommand);
        }
    }

    private String lookCommand() {
        this.gameLocations = this.gameController.getFromGameLocations(this.currentPlayerLocation);
        StringBuilder outputText = new StringBuilder();

        if (this.gameLocations != null) {
            outputText.append("You are at ")
                    .append(this.gameLocations.getDescription())
                    .append(".\n");

            Map<String, GameArtefacts> artefacts = this.gameController.getArtefactsAtLocation(this.currentPlayerLocation);
            Map<String, GameFurniture> furniture = this.gameController.getFurnitureAtLocation(this.currentPlayerLocation);
            Map<String, GameCharacters> characters = this.gameController.getCharactersAtLocation(this.currentPlayerLocation);
            GamePlayers otherPlayers = this.gameController.getPlayersAtLocation(this.currentPlayerName, this.currentPlayerLocation);

            if (artefacts != null && furniture != null && characters != null && otherPlayers != null) {
                outputText.append("You can see: \n");
            }

            if (artefacts != null) {
                for (GameArtefacts artefact : artefacts.values()) {
                    outputText.append(" ")
                            .append(artefact.getDescription())
                            .append("\n");
                }
            }

            if (furniture != null) {
                for (GameFurniture gameFurniture : furniture.values()) {
                    outputText.append(" ")
                            .append(gameFurniture.getDescription())
                            .append("\n");
                }
            }

            if (characters != null) {
                for (GameCharacters character : characters.values()) {
                    outputText.append(" ")
                            .append(character.getDescription())
                            .append("\n");
                }
            }

            if (otherPlayers != null) {
                outputText.append(" Player: ")
                            .append(otherPlayers.getPlayerName())
                            .append("\n");
            }

            LinkedList<String> destinations = this.gameLocations.getNextDestination();
            if (destinations != null && !destinations.isEmpty()) {
                outputText.append("You can access from here: \n");
                for (String destination : destinations) {
                    outputText.append(" ").append(destination).append("\n");
                }
            }
        }
        return outputText.toString();
    }

    private String goToCommand(String command) {
        this.gameLocations = this.gameController.getFromGameLocations(this.currentPlayerLocation);
        if (this.gameLocations == null) {
            return "Current location is not found.";
        }

        LinkedList<String> allowedLocation = this.gameLocations.getNextDestination();
        Pattern goToPattern = Pattern.compile(".*\\bgoto\\b\\s+(.*)$");
        Matcher goToMatcher = goToPattern.matcher(command);

        if (goToMatcher.matches()) {
            String inputLocation = goToMatcher.group(1).trim();
            if (allowedLocation.contains(inputLocation)) {
                this.currentPlayerLocation = inputLocation;
                gameController.setCurrentLocations(this.currentPlayerName, inputLocation);
                return this.lookCommand();
            } else if (inputLocation.equals(this.currentPlayerLocation)) {
                return "Already at destination.\n";
            }
        }
        return "Unable to go there.\n";
    }

    private String inventoryCommand() {
        Map<String, GameArtefacts> availableArtefacts = gameController.getCurrentInventory(this.currentPlayerName);
        StringBuilder outputText = new StringBuilder();
        if (!availableArtefacts.isEmpty()) {
            System.out.println(availableArtefacts.size());
            outputText.append("Current Items in Inventory: \n");
            for (GameArtefacts artefact : availableArtefacts.values()) {
                outputText.append(" ")
                        .append(artefact.getDescription()).append("\n");
            }
        } else {
            outputText.append("No items in inventory.\n");
        }
        return outputText.toString();
    }

    private String getCommand(String command) {
        Pattern getPattern = Pattern.compile(".*\\bget\\b\\s+(.*)$");
        Matcher getMatcher = getPattern.matcher(command);

        Map<String, GameArtefacts> allowedArtefacts = gameController.getArtefactsAtLocation(this.currentPlayerLocation);
        StringBuilder outputText = new StringBuilder();

        if (getMatcher.matches()) {
            String artefactName = getMatcher.group(1).trim();
                if (allowedArtefacts.containsKey(artefactName)) {
                    GameArtefacts artefactObject = allowedArtefacts.get(artefactName);
                    gameController.pickUpArtefactsAtLocation(this.currentPlayerName, this.currentPlayerLocation, artefactObject);
                    outputText.append("Picked up ")
                            .append(artefactObject.getDescription())
                            .append(".\n");
                }
                else {
                    return "That artefact is not in here.\n";
                }
        }
        return outputText.toString();
    }

    private String dropCommand(String command) {
        Pattern dropPattern = Pattern.compile(".*\\bdrop\\b\\s+(.*)$");
        Matcher dropMatcher = dropPattern.matcher(command);

        Map<String, GameArtefacts> artefactsInInventory = gameController.getCurrentInventory(this.currentPlayerName);
        StringBuilder outputText = new StringBuilder();

        if (dropMatcher.matches()) {
            String artefactName = dropMatcher.group(1).trim();
            if (artefactsInInventory.containsKey(artefactName)) {
                GameArtefacts artefactObject = artefactsInInventory.get(artefactName);
                gameController.dropArtefactAtLocation(this.currentPlayerName, this.currentPlayerLocation, artefactObject);
                outputText.append("Dropped ")
                        .append(artefactObject.getDescription())
                        .append(".\n");
            } else {
                return "You don't have that it in your inventory.\n";
            }
        }
        return outputText.toString();
    }
}