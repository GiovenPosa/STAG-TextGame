package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.PortNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LoadEntityFile {
    private final Map<String, GameLocations> allLocations = new LinkedHashMap<>();
    private final Map<String, Map<String, GameArtefacts>> locationArtefacts = new HashMap<>();
    private final Map<String, Map<String, GameFurniture>> locationFurniture = new HashMap<>();
    private final Map<String, Map<String, GameCharacters>> locationCharacters = new HashMap<>();

    public LoadEntityFile(File entitiesFile) {
        this.parseLocations(entitiesFile);
        this.parsePaths(entitiesFile);
    }

    private Graph getGraphsInDot(File entitiesFile) {
        Parser p = null;
        try {
            FileReader fr = new FileReader(entitiesFile);
            p = new Parser();
            p.parse(fr);
        } catch (FileNotFoundException | ParseException e) {
            e.getCause();
        }
        return Objects.requireNonNull(p).getGraphs().get(0);
    }

    private void parseLocations(File entitiesFile) {
        Graph subGraphs = this.getGraphsInDot(entitiesFile);

        Graph locations = null;
        for (Graph subgraph : subGraphs.getSubgraphs()) {
            String subgraphID = subgraph.getId().getId();
            if ("locations".equals(subgraphID)) {
                locations = subgraph;
            }
        }

        if (!Objects.requireNonNull(locations).getSubgraphs().isEmpty()) {
            for (Graph cluster : locations.getSubgraphs()) {
                String clusterID = cluster.getId().getId();
                if (clusterID.startsWith("cluster")) {
                    this.getGameLocations(cluster);
                }
            }
        }
    }

    private void getGameLocations(Graph cluster) {
        Node locationNode = null;
        for (Node node : cluster.getNodes(false)) {
            locationNode = node;
            break;
        }

        if (locationNode != null) {
            String locationName = locationNode.getId().getId();
            String locationDescription = locationNode.getAttribute("description");
            GameLocations location = new GameLocations(locationName, locationDescription);
            allLocations.put(locationName, location);
            this.parseLocationItems(locationName, cluster);
        }
    }

    private void parseLocationItems(String locationName, Graph cluster) {
        if (!cluster.getSubgraphs().isEmpty() && cluster.getSubgraphs() != null) {
            for (Graph subgraph : cluster.getSubgraphs()) {
                String subgraphID = subgraph.getId().getId();
                if ("artefacts".equals(subgraphID)) {
                    GameArtefactFactory artefactFactory = new GameArtefactFactory(allLocations);
                    Map<String, GameArtefacts> artefacts = this.getLocationItems(subgraph, artefactFactory);
                    locationArtefacts.put(locationName, artefacts);
                }
                if ("furniture".equals(subgraphID)) {
                    GameFurnitureFactory furnitureFactory = new GameFurnitureFactory(allLocations);
                    Map<String, GameFurniture> furniture = this.getLocationItems(subgraph, furnitureFactory);
                    locationFurniture.put(locationName, furniture);
                }
                if ("characters".equals(subgraphID)) {
                    GameCharacterFactory characterFactory = new GameCharacterFactory(allLocations);
                    Map<String, GameCharacters> characters = this.getLocationItems(subgraph, characterFactory);
                    locationCharacters.put(locationName, characters);
                }
            }
        }
    }

    private <T extends GameEntity> Map<String, T> getLocationItems(Graph subgraph, GameItemFactory<T> factory) {
        Map<String, T> gameItems = new HashMap<>();

        for (Node itemNode : subgraph.getNodes(false)) {
            String itemName = itemNode.getId().getId();
            String itemDescription = itemNode.getAttribute("description");
            T gameItem = factory.create(itemName, itemDescription, allLocations);
            gameItems.put(itemName, gameItem);
        }
        return gameItems;
    }

    private void parsePaths(File entitiesFile) {
        Graph subGraphs = this.getGraphsInDot(entitiesFile);

        for (Graph paths : subGraphs.getSubgraphs()) {
            String subgraphID = paths.getId().getId();
            if ("paths".equals(subgraphID)) {
                this.getGamePaths(paths);
            }
        }
    }

    private void getGamePaths(Graph paths) {
        for (Edge pathEdge : paths.getEdges()) {
            PortNode currentLocationPort = pathEdge.getSource();
            Node currentLocationNode = currentLocationPort.getNode();
            String currentLocationName = currentLocationNode.getId().getId();

            PortNode nextLocationPort = pathEdge.getTarget();
            Node nextLocationNode = nextLocationPort.getNode();
            String nextLocationName = nextLocationNode.getId().getId();

            GameLocations currentLocation = allLocations.get(currentLocationName);
            if (currentLocation != null) {
                currentLocation.addNextDestination(nextLocationName);
            }
        }
    }

    public GameLocations getLocation(String locationName) {
        return this.allLocations.get(locationName);
    }

    public Map<String, GameLocations> getAllLocations() {
        return this.allLocations;
    }

    public Map<String, GameArtefacts> getArtefacts(String locationName) {
        return this.locationArtefacts.get(locationName);
    }

    public Map<String, GameFurniture> getFurnitures(String locationName) {
        return this.locationFurniture.get(locationName);
    }

    public Map<String, GameCharacters> getCharacters(String locationName) {
        return this.locationCharacters.get(locationName);
    }

    public void removeArtefact(String locationName, GameArtefacts usedArtefact) {
       Map<String, GameArtefacts> artefactsMap = this.locationArtefacts.get(locationName);

       if (artefactsMap != null) {
           artefactsMap.remove(usedArtefact.getName());
       }
    }

    public void removeFurniture(String locationName, GameFurniture furniture) {
        Map<String, GameFurniture> furnitureMap = this.locationFurniture.get(locationName);

        if (furnitureMap != null) {
            furnitureMap.remove(furniture.getName());
        }
    }

    public void removeCharacter(String locationName, GameCharacters character) {
        Map<String, GameCharacters> charactersMap = this.locationCharacters.get(locationName);

        if (charactersMap != null) {
            charactersMap.remove(character.getName());
        }
    }

    public void addArtefact(String locationName, GameArtefacts droppedArtefact) {
       if (!this.locationArtefacts.containsKey(locationName)) {
           this.locationArtefacts.put(locationName, new LinkedHashMap<>());
       }
       this.locationArtefacts.get(locationName).put(droppedArtefact.getName(), droppedArtefact);
    }

    public void addFurniture(String locationName, GameFurniture furniture) {
        if (!this.locationFurniture.containsKey(locationName)) {
            this.locationFurniture.put(locationName, new LinkedHashMap<>());
        }
        this.locationFurniture.get(locationName).put(furniture.getName(), furniture);
    }

    public void addCharacter(String locationName, GameCharacters gameCharacter) {
        if (!this.locationCharacters.containsKey(locationName)) {
            this.locationCharacters.put(locationName, new LinkedHashMap<>());
        }
        this.locationCharacters.get(locationName).put(gameCharacter.getName(), gameCharacter);
    }

}
