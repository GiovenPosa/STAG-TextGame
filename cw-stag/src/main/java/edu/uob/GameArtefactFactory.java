package edu.uob;

import java.util.Map;

public class GameArtefactFactory implements GameItemFactory<GameArtefacts> {
    private final Map<String, GameLocations> locationMap;

    public GameArtefactFactory(Map<String, GameLocations> locationMap) {
        this.locationMap = locationMap;
    }

    @Override
    public GameArtefacts create(String name, String description, Map<String, GameLocations> locationMap) {
        return new GameArtefacts(name, description, this.locationMap);
    }
}
