package edu.uob;

import java.util.Map;

public class GameFurniture extends GameEntity {

    protected String name;
    protected String description;
    Map<String, GameLocations> location;

    public GameFurniture (String name, String description, Map<String, GameLocations> location) {
        super(name, description);
        this.location = location;
    }
}
