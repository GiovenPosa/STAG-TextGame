package edu.uob;

import java.util.Map;

public interface GameItemFactory<T extends GameEntity> {
    T create (String name, String description, Map<String, GameLocations> locationMap);
}
