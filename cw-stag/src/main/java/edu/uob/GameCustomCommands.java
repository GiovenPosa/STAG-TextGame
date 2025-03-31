package edu.uob;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class GameCustomCommands {
    private final GameController gameController;
    private String currentPlayerName;
    private String currentPlayerLocation;

    public GameCustomCommands(GameController gameController) {
        this.gameController = gameController;
    }

    public String getCustomCommands(String playerName, String playerCommand) {
        GamePlayers currentPlayer = this.gameController.getOrCreatePlayer(playerName);
        this.currentPlayerName = currentPlayer.getPlayerName();
        this.currentPlayerLocation = this.gameController.getCurrentLocations(this.currentPlayerName);
        return this.getGameActionCommand(playerCommand);
    }

    private String getGameActionCommand(String playerCommand) {
        LinkedList<GameAction> gameActions = gameController.getAllGameActions();
        LinkedList<String> customParts = new LinkedList<>(Arrays.asList(playerCommand.split(" ")));
        if (customParts.isEmpty()) {
            return "Invalid command.\n";
        }

        for (GameAction gameAction : gameActions) {
            boolean hasTriggerPhrase = false;
            boolean hasSubjectEntity = false;
            for (String triggerPhrase : gameAction.getTriggerPhrases()) {
                if (containsPhrase(playerCommand, triggerPhrase)) {
                    hasTriggerPhrase = true;
                    break;
                }
            }
            for (String subjectEntity : gameAction.getSubjectEntities()) {
                if (containsPhrase(playerCommand, subjectEntity)) {
                    hasSubjectEntity = true;
                    break;
                }
            }
            if (hasTriggerPhrase && hasSubjectEntity) {
                return this.outputProducedActionText(gameAction);
            }
        }
        return "Not a valid action!\n";
    }

    private boolean containsPhrase(String playerCommand, String keyphrase) {
        LinkedList<String> phrases = new LinkedList<>(Arrays.asList(playerCommand.split(" ")));
        LinkedList<String> words = new LinkedList<>(Arrays.asList(keyphrase.split(" ")));
        if (phrases.isEmpty() || words.isEmpty()) {
            return false;
        }
        int wordIndex = 0;
        for (String phrase : phrases) {
            if (phrase.equals(words.get(wordIndex))) {
                wordIndex++;
                if (wordIndex == words.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String outputProducedActionText(GameAction gameAction) {
        boolean isInLocation = this.isSubjectEntityInLocation(gameAction);
        boolean isInInventory = this.isSubjectEntityInPlayerInventory(gameAction);

        if (!isInLocation) {
            return "We cant do that in this location!\n";
        }
        else if (!isInInventory) {
            return "Missing things in inventory!\n";
        }
        gameController.produceNewEntity(this.currentPlayerName, this.currentPlayerLocation, gameAction);
        gameController.consumeEntity(this.currentPlayerName, this.currentPlayerLocation, gameAction);
        return gameAction.outputProducedText();

    }

    private boolean isSubjectEntityInLocation(GameAction gameAction) {
        Map<String, GameFurniture> furnituresInLocation = gameController.getFurnitureAtLocation(this.currentPlayerLocation);
        LinkedList<String> subjectEntities = new LinkedList<>(gameAction.getSubjectEntities());

        if (furnituresInLocation != null && !furnituresInLocation.isEmpty()) {
            for (GameFurniture gameFurniture : furnituresInLocation.values()) {
                String furnitureName = gameFurniture.getName().toLowerCase();
                for (String subjectEntity : subjectEntities) {
                    if (furnitureName.equals(subjectEntity)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSubjectEntityInPlayerInventory(GameAction gameAction) {
        Map<String, GameArtefacts> playersInventory = gameController.getCurrentInventory(this.currentPlayerName);
        Map<String, GameArtefacts> artefactsInLocation = gameController.getArtefactsAtLocation(this.currentPlayerLocation);
        LinkedList<String> subjectEntities = new LinkedList<>(gameAction.getSubjectEntities());

        if (checkArtefact(playersInventory, subjectEntities)) return true;
        return checkArtefact(artefactsInLocation, subjectEntities);
    }

    private boolean checkArtefact(Map<String, GameArtefacts> artefactsIn, LinkedList<String> subjectEntities) {
        if (artefactsIn != null && !artefactsIn.isEmpty()) {
            for (GameArtefacts gameArtefacts : artefactsIn.values()) {
                String artefactName = gameArtefacts.getName().toLowerCase();
                for (String subjectEntity : subjectEntities) {
                    if (artefactName.equals(subjectEntity)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
