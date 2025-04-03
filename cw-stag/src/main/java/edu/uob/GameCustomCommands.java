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

        int matchCount = 0;
        GameAction matchedAction = null;

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
                matchCount++;
                matchedAction = gameAction;
            }
        }

        if (matchCount > 1) {
            return "Multiple actions is available. Try do one action or be more specific.\n";
        } else if (matchCount == 1) {
            return this.outputProducedActionText(matchedAction);
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
        int currentHealth = gameController.getCurrentPlayerHealth(this.currentPlayerName);
        if (currentHealth <= 1) {
            GamePlayers currentPlayer = this.gameController.getOrCreatePlayer(this.currentPlayerName);
            return gameController.restartPlayerState(this.currentPlayerLocation, currentPlayer);
        }

        LinkedList<String> requiredEntities = gameAction.getSubjectEntities();
        LinkedList<String> availableEntities = new LinkedList<>();

        availableEntities.addAll(this.isFurnitureEntityAvailable(gameAction));
        availableEntities.addAll(this.isArtefactEntityAvailable(gameAction));
        availableEntities.addAll(this.isCharacterEntityAvailable(gameAction));

        for (String requiredEntity : requiredEntities) {
            boolean found = false;
            for (String availableEntity : availableEntities) {
                if (requiredEntity.equalsIgnoreCase(availableEntity)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return "Missing required entities to complete the action!\n";
            }
        }
        gameController.consumeEntity(this.currentPlayerName, this.currentPlayerLocation, gameAction);
        gameController.produceEntity(this.currentPlayerName, this.currentPlayerLocation, gameAction);
        return gameAction.outputProducedText();

    }

    private LinkedList<String> isFurnitureEntityAvailable(GameAction gameAction) {
        Map<String, GameFurniture> furnituresInLocation = gameController.getFurnitureAtLocation(this.currentPlayerLocation);
        LinkedList<String> subjectEntities = new LinkedList<>(gameAction.getSubjectEntities());
        LinkedList<String> matchedFurniture = new LinkedList<>();

        if (furnituresInLocation != null && !furnituresInLocation.isEmpty()) {
            for (GameFurniture gameFurniture : furnituresInLocation.values()) {
                String furnitureName = gameFurniture.getName().toLowerCase();
                for (String subjectEntity : subjectEntities) {
                    if (furnitureName.equalsIgnoreCase(subjectEntity)) {
                        matchedFurniture.add(furnitureName);
                    }
                }
            }
        }
        return matchedFurniture;
    }

    private LinkedList<String> isCharacterEntityAvailable(GameAction gameAction) {
        Map<String, GameCharacters> characterInLocation = gameController.getCharactersAtLocation(this.currentPlayerLocation);
        LinkedList<String> subjectEntities = new LinkedList<>(gameAction.getSubjectEntities());
        LinkedList<String> matchedCharacters = new LinkedList<>();

        if (characterInLocation != null && !characterInLocation.isEmpty()) {
            for (GameCharacters gameCharacter : characterInLocation.values()) {
                String characterName = gameCharacter.getName().toLowerCase();
                for (String subjectEntity : subjectEntities) {
                    if (characterName.equalsIgnoreCase(subjectEntity)) {
                        matchedCharacters.add(characterName);
                    }
                }
            }
        }
        return matchedCharacters;
    }

    private LinkedList<String> isArtefactEntityAvailable(GameAction gameAction) {
        Map<String, GameArtefacts> playersInventory = gameController.getCurrentInventory(this.currentPlayerName);
        Map<String, GameArtefacts> artefactsInLocation = gameController.getArtefactsAtLocation(this.currentPlayerLocation);
        LinkedList<String> subjectEntities = new LinkedList<>(gameAction.getSubjectEntities());
        LinkedList<String> matchedArtefacts = new LinkedList<>();

        matchedArtefacts.addAll(this.checkArtefact(playersInventory, subjectEntities));
        matchedArtefacts.addAll(this.checkArtefact(artefactsInLocation, subjectEntities));
        return matchedArtefacts;
    }

    private LinkedList<String> checkArtefact(Map<String, GameArtefacts> artefactsIn, LinkedList<String> subjectEntities) {
        LinkedList<String> matchedArtefacts = new LinkedList<>();

        if (artefactsIn != null && !artefactsIn.isEmpty()) {
            for (GameArtefacts gameArtefacts : artefactsIn.values()) {
                String artefactName = gameArtefacts.getName().toLowerCase();
                for (String subjectEntity : subjectEntities) {
                   if(artefactName.equalsIgnoreCase(subjectEntity)) {
                       matchedArtefacts.add(artefactName);
                   }
                }
            }
        }
        return matchedArtefacts;
    }
}
