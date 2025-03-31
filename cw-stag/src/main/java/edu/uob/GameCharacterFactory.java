package edu.uob;

import java.util.Map;

public class GameCharacterFactory implements GameItemFactory<GameCharacters> {
    private final Map<String, GameLocations> locationMap;

    public GameCharacterFactory(Map<String, GameLocations> locationMap) {
        this.locationMap = locationMap;
    }

    @Override
    public GameCharacters create(String name, String description, Map<String, GameLocations> locationMap) {
        return new GameCharacters(name, description, this.locationMap);
    }
}
