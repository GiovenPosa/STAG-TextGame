package edu.uob;

import java.util.Map;

public class GameFurnitureFactory implements GameItemFactory<GameFurniture> {
    private final Map<String, GameLocations> locationMap;

    public GameFurnitureFactory(Map<String, GameLocations> locationMap) {
        this.locationMap = locationMap;
    }

    @Override
    public GameFurniture create(String name, String description, Map<String, GameLocations> locationMap) {
        return new GameFurniture(name, description, this.locationMap);
    }
}