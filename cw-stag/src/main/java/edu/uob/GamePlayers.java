package edu.uob;

import java.util.HashMap;
import java.util.Map;

public class GamePlayers {
    private final String playerName;
    private String playerLocation;
    Map<String, GameArtefacts> playerInventory = new HashMap<>();

    public GamePlayers(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getPlayerLocation() {
        return this.playerLocation;
    }

    public void setPlayerLocation(String playerLocation) {
        this.playerLocation = playerLocation;
    }

    public Map<String, GameArtefacts> getPlayerInventory() {
        return this.playerInventory;
    }

    public void pickUpArtefacts(GameArtefacts desiredArtefact) {
        this.playerInventory.put(desiredArtefact.getName(), desiredArtefact);
    }

    public void dropArtefact(GameArtefacts desiredArtefact) {
        this.playerInventory.remove(desiredArtefact.getName());
    }
}
