package edu.uob;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GameController {
    private final LoadEntityFile loadEntityFile;
    private final LoadActionFile loadActionFile;
    private final Map<String, GamePlayers> gamePlayer = new HashMap<>();
    private final GameCommands gameCommands;

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
         loadEntityFile.removeArtefact(currentLocation, artefactObject);
    }

    public void dropArtefactAtLocation(String playerName, String currentLocation, GameArtefacts artefactObject) {
        this.getOrCreatePlayer(playerName).dropArtefact(artefactObject);
        loadEntityFile.addArtefact(currentLocation, artefactObject);
    }

    public void produceNewEntity(String playerName, String currentLocation, GameAction gameAction) {
        LinkedList<String> producedEntities = new LinkedList<>(gameAction.getProducedEntities());

        String producedName = producedEntities.get(0);
        String producedDescription = producedEntities.get(0);
        Map<String, GameLocations> allLocations = this.getAllLocations();

        boolean updatedNextDestination = false;

        for (GameLocations gameLocations : allLocations.values()) {
            String locationName = gameLocations.getName();
            for (String producedEntity : producedEntities) {
                if(producedEntity.contains(locationName)) {
                    GameLocations currentGameLocation = allLocations.get(currentLocation);
                    currentGameLocation.addNextDestination(producedName);
                    updatedNextDestination = true;
                    break;
                }
            }
            if (updatedNextDestination) {
                break;
            }
        }

        if (!updatedNextDestination) {
            GameArtefactFactory artefactFactory = new GameArtefactFactory(allLocations);
            GameArtefacts newArtefact = artefactFactory.create(producedName, producedDescription, allLocations);
            this.dropArtefactAtLocation(playerName, currentLocation, newArtefact);
        }
    }

    public void consumeEntity(String playerName, String currentLocation, GameAction gameAction) {
        LinkedList<String> consumedEntities = new LinkedList<>(gameAction.getConsumedEntities());
        String consumedName = consumedEntities.get(0);

        Map<String, GameArtefacts> playerInventory = this.getCurrentInventory(playerName);
        Map<String, GameArtefacts> artefactsInLocation = this.getArtefactsAtLocation(currentLocation);
        Map<String, GameFurniture> furnitureInLocation = this.getFurnitureAtLocation(currentLocation);

        for (GameArtefacts playerArtefacts : playerInventory.values()) {
            String playerArtefactName = playerArtefacts.getName();
            if (playerArtefactName.equals(consumedName)) {
                this.getOrCreatePlayer(playerName).dropArtefact(playerArtefacts);
            }
        }
        for (GameArtefacts artefacts : artefactsInLocation.values()) {
            String artefactName = artefacts.getName();
            if (artefactName.equals(consumedName)) {
                this.loadEntityFile.removeArtefact(currentLocation, artefacts);
            }
        }
        for (GameFurniture furniture : furnitureInLocation.values()) {
            String furnitureName = furniture.getName();
            if (furnitureName.equals(consumedName)) {
                this.loadEntityFile.removeFurniture(currentLocation, furniture);
            }
        }
    }

    private String getFirstLocation() {
       Map<String, GameLocations> locations = loadEntityFile.getAllLocations();
       if (locations != null && !locations.isEmpty()) {
           GameLocations startLocation = locations.values().iterator().next();
           return startLocation.getName();
       } else {
           return "No locations found.";
       }
    }
}