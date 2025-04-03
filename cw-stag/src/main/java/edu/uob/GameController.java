package edu.uob;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GameController {
    private final LoadEntityFile loadEntityFile;
    private final LoadActionFile loadActionFile;
    private final Map<String, GamePlayers> gamePlayer = new HashMap<>();
    private final GameCommands gameCommands;
    private final Integer maxHealth = 3;

    public GameController(LoadEntityFile loadEntityFile, LoadActionFile loadActionFile) {
        this.loadEntityFile = loadEntityFile;
        this.loadActionFile = loadActionFile;
        this.gameCommands = new GameCommands(this);
    }

    public String getCommandFromServer(String command) {
        return gameCommands.checkCommand(command);
    }

    public GamePlayers getOrCreatePlayer(String playerName) {
        if (!gamePlayer.containsKey(playerName)) {
            GamePlayers newPlayer = new GamePlayers(playerName);
            newPlayer.setPlayerLocation(getFirstLocation());
            gamePlayer.put(playerName, newPlayer);
        }
        return gamePlayer.get(playerName);
    }

    public LinkedList<GameAction> getAllGameActions() {
        return loadActionFile.getGameActions();
    }

    public Map<String, GameLocations> getAllLocations() {
        return loadEntityFile.getAllLocations();
    }

    public GameLocations getFromGameLocations(String selectedLocation) {
        return loadEntityFile.getLocation(selectedLocation);
    }

    public Map<String, GameArtefacts> getArtefactsAtLocation(String selectedLocation) {
        return loadEntityFile.getArtefacts(selectedLocation);
    }

    public Map<String, GameFurniture> getFurnitureAtLocation(String selectedLocation) {
        return loadEntityFile.getFurnitures(selectedLocation);
    }

    public Map<String, GameCharacters> getCharactersAtLocation(String selectedLocation) {
        return loadEntityFile.getCharacters(selectedLocation);
    }

    public GamePlayers getPlayersAtLocation(String currentPlayerName, String selectedLocation) {
        for (GamePlayers gamePlayer : gamePlayer.values()) {
            if (!gamePlayer.getPlayerName().equals(currentPlayerName)) {
                if (gamePlayer.getPlayerLocation().equals(selectedLocation)) {
                    return gamePlayer;
                }
            }
        }
        return null;
    }

    public Integer getCurrentPlayerHealth(String playerName) {
        return this.gamePlayer.get(playerName).getPlayerHealth();
    }

    public void setCurrentPlayerHealth(String playerName, Integer newHealth) {
        this.gamePlayer.get(playerName).setPlayerHealth(newHealth);
    }

    public String getCurrentLocations(String playerName) {
        return this.getOrCreatePlayer(playerName).getPlayerLocation();
    }

    public void setCurrentLocations(String playerName, String goToLocation) {
        this.getOrCreatePlayer(playerName).setPlayerLocation(goToLocation);
    }

    public Map<String, GameArtefacts> getCurrentInventory(String playerName) {
        return this.getOrCreatePlayer(playerName).getPlayerInventory();
    }

    public void pickUpArtefactsAtLocation(String playerName, String currentLocation, GameArtefacts artefactObject) {
         this.getOrCreatePlayer(playerName).pickUpArtefacts(artefactObject);
         this.loadEntityFile.removeArtefact(currentLocation, artefactObject);
    }

    public void dropArtefactAtLocation(String playerName, String currentLocation, GameArtefacts artefactObject) {
        this.getOrCreatePlayer(playerName).removeArtefact(artefactObject);
        this.loadEntityFile.addArtefact(currentLocation, artefactObject);
    }

    public void produceEntity(String playerName, String currentLocation, GameAction gameAction) {
        LinkedList<String> producedEntities = new LinkedList<>(gameAction.getProducedEntities());

        GamePlayers currentPlayer = this.getOrCreatePlayer(playerName);
        Map<String, GameArtefacts> artefactsInStoreroom = this.getArtefactsAtLocation("storeroom");
        Map<String, GameFurniture> furnitureInStoreroom = this.getFurnitureAtLocation("storeroom");
        Map<String, GameCharacters> charactersInStoreroom = this.getCharactersAtLocation("storeroom");
        Map<String, GameLocations> gameLocations = this.getAllLocations();

        if (producedEntities.isEmpty()) {
            return;
        }

        for (String producedEntity : producedEntities) {
            if (producedEntity.equalsIgnoreCase("health")) {
                int currentHealth = currentPlayer.getPlayerHealth();
                if (currentHealth >= this.maxHealth) {
                    this.setCurrentPlayerHealth(playerName, this.maxHealth);
                    continue;
                }
                this.setCurrentPlayerHealth(playerName, currentHealth + 1);

            }
            if (artefactsInStoreroom != null) {
                for (GameArtefacts artefact : artefactsInStoreroom.values()) {
                    if (artefact.getName().equals(producedEntity)) {
                        this.loadEntityFile.addArtefact(currentLocation, artefact);
                        this.loadEntityFile.removeArtefact("storeroom", artefact);
                        break;
                    }
                }
            }
            if (furnitureInStoreroom != null) {
                for (GameFurniture furniture : furnitureInStoreroom.values()) {
                    if (furniture.getName().equals(producedEntity)) {
                        this.loadEntityFile.addFurniture(currentLocation, furniture);
                        this.loadEntityFile.removeFurniture("storeroom", furniture);
                        break;
                    }
                }
            }
            if (charactersInStoreroom != null) {
                for (GameCharacters character : charactersInStoreroom.values()) {
                    if (character.getName().equals(producedEntity)) {
                        this.loadEntityFile.addCharacter(currentLocation, character);
                        this.loadEntityFile.removeCharacter("storeroom", character);
                        break;
                    }
                }
            }
            if (gameLocations != null) {
                for (GameLocations gameLocation : gameLocations.values()) {
                    if (gameLocation.getName().equalsIgnoreCase(producedEntity)) {
                        GameLocations currentGameLocation = this.getAllLocations().get(currentLocation);
                        currentGameLocation.addNextDestination(producedEntity);
                        break;
                    }
                }
            }
        }
    }

    public String restartPlayerState(String currentLocation, GamePlayers currentPlayer) {
        Map<String, GameArtefacts> playerInventory = currentPlayer.getPlayerInventory();
        if (playerInventory != null) {
            for (GameArtefacts artefact : playerInventory.values()) {
                this.loadEntityFile.addArtefact(currentLocation, artefact);
            }
            playerInventory.clear();
        }
        String startLocation = this.getFirstLocation();
        currentPlayer.setPlayerLocation(startLocation);
        currentPlayer.setPlayerHealth(this.maxHealth);
        return "You have died. Restarting at first location!\n";
    }

    public void consumeEntity(String playerName, String currentLocation, GameAction gameAction) {
        LinkedList<String> consumedEntities = new LinkedList<>(gameAction.getConsumedEntities());

        GamePlayers currentPlayer = this.getOrCreatePlayer(playerName);
        Map<String, GameArtefacts> playerInventory = currentPlayer.getPlayerInventory();
        Map<String, GameArtefacts> artefactsInLocation = this.getArtefactsAtLocation(currentLocation);
        Map<String, GameFurniture> furnitureInLocation = this.getFurnitureAtLocation(currentLocation);
        Map<String, GameCharacters> charactersInLocation = this.getCharactersAtLocation(currentLocation);

        for (String consumedEntityName : consumedEntities) {
            if (consumedEntityName.equalsIgnoreCase("health")) {
                int currentHealth = currentPlayer.getPlayerHealth();
                this.setCurrentPlayerHealth(playerName, currentHealth - 1);
                continue;
            }
            if (playerInventory != null) {
                for (GameArtefacts playerInventoryArtefact : playerInventory.values()) {
                    if (playerInventoryArtefact.getName().equalsIgnoreCase(consumedEntityName)) {
                       this.loadEntityFile.addArtefact("storeroom", playerInventoryArtefact);
                        currentPlayer.removeArtefact(playerInventoryArtefact);
                        break;
                    }
                }
            }
            if (artefactsInLocation != null) {
                for (GameArtefacts artefacts : artefactsInLocation.values()) {
                    if (artefacts.getName().equalsIgnoreCase(consumedEntityName)) {
                        this.loadEntityFile.addArtefact("storeroom", artefacts);
                        this.loadEntityFile.removeArtefact(currentLocation, artefacts);
                        break;
                    }
                }
            }
            if (furnitureInLocation != null) {
                for (GameFurniture furniture : furnitureInLocation.values()) {
                    if (furniture.getName().equalsIgnoreCase(consumedEntityName)) {
                        this.loadEntityFile.addFurniture("storeroom", furniture);
                        this.loadEntityFile.removeFurniture(currentLocation, furniture);
                        break;
                    }
                }
            }
            if (charactersInLocation != null) {
                for (GameCharacters characters : charactersInLocation.values()) {
                    if (characters.getName().equalsIgnoreCase(consumedEntityName)) {
                        this.loadEntityFile.addCharacter("storeroom", characters);
                        this.loadEntityFile.removeCharacter(currentLocation, characters);
                        break;
                    }
                }
            }
        }
    }

    private String getFirstLocation() {
       Map<String, GameLocations> locations = this.loadEntityFile.getAllLocations();
       if (locations != null && !locations.isEmpty()) {
           GameLocations startLocation = locations.values().iterator().next();
           return startLocation.getName();
       } else {
           return "No locations found.";
       }
    }
}