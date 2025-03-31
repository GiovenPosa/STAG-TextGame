package edu.uob;

import java.util.LinkedList;

public class GameAction {
    private final LinkedList<String> triggerPhrases;
    private final LinkedList<String> subjectEntities;
    private final LinkedList<String> consumedEntities;
    private final LinkedList<String> producedEntities;
    private final LinkedList<String> narration;

    public GameAction() {
        this.triggerPhrases = new LinkedList<>();
        this.subjectEntities = new LinkedList<>();
        this.consumedEntities = new LinkedList<>();
        this.producedEntities = new LinkedList<>();
        this.narration = new LinkedList<>();
    }

    public LinkedList<String> getTriggerPhrases() {
        return triggerPhrases;
    }

    public LinkedList<String> getSubjectEntities() {
        return subjectEntities;
    }

    public LinkedList<String> getConsumedEntities() {
        return consumedEntities;
    }

    public LinkedList<String> getProducedEntities() {
        return producedEntities;
    }

    public LinkedList<String> getNarration() {
        return narration;
    }

    public String outputProducedText() {
        StringBuilder outputText = new StringBuilder();
        outputText.append("You have produced: ")
                .append(getProducedEntities().toString()).append("\n")
                .append(getNarration().toString()).append("\n");
        return outputText.toString();
    }
}
