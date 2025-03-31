package edu.uob;

import java.util.Map;

public class GameArtefacts extends GameEntity{

    protected String name;
    protected String description;
    Map<String, GameLocations> location;

    public GameArtefacts (String name, String description, Map<String, GameLocations> location) {
        super(name, description);
        this.location = location;
    }


}
