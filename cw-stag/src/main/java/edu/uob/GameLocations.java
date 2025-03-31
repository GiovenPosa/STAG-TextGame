package edu.uob;

import java.util.LinkedList;

public class GameLocations extends GameEntity {
    private final LinkedList<String> nextDestinations;

    public GameLocations(String name, String description) {
        super(name, description);
        this.nextDestinations = new LinkedList<>();
    }

    public void addNextDestination(String nextDestination) {
        if (nextDestination != null && !nextDestination.isEmpty()) {
            this.nextDestinations.add(nextDestination);
        }
    }

    public LinkedList<String> getNextDestination() {
        return this.nextDestinations;
    }
}
