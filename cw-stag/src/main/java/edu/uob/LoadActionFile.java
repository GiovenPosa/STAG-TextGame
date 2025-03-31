package edu.uob;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class LoadActionFile {
    private final File actionsFile;
    private final LinkedList<GameAction> gameActions;

    public LoadActionFile(File actionsFile) {
        this.actionsFile = actionsFile;
        this.gameActions = new LinkedList<>();
        this.parseActionsFile();
    }

    public void parseActionsFile() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document actionsDocument = builder.parse(this.actionsFile);
            Element rootElement = actionsDocument.getDocumentElement();
            NodeList actions = rootElement.getElementsByTagName("action");

            for (int i = 0; i < actions.getLength(); i++) {
                Element actionElement = (Element) actions.item(i);
                GameAction gameAction = new GameAction();

                this.getSubElements(actionElement, "triggers", "keyphrase", gameAction.getTriggerPhrases());
                this.getSubElements(actionElement, "subjects", "entity", gameAction.getSubjectEntities());
                this.getSubElements(actionElement, "consumed", "entity", gameAction.getConsumedEntities());
                this.getSubElements(actionElement, "produced", "entity", gameAction.getProducedEntities());
                this.getNarrationElement(actionElement, gameAction.getNarration());
                this.gameActions.add(gameAction);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getNarrationElement(Element elementType, LinkedList<String> elementCollection) {
        Element narration = (Element)elementType.getElementsByTagName("narration").item(0);
        if (narration != null) {
            String narrationString = narration.getTextContent();
            elementCollection.add(narrationString);
        }
    }

    private void getSubElements(Element elementType, String elementName, String subElementName, LinkedList<String> elementCollection) {
        Element element = (Element)elementType.getElementsByTagName(elementName).item(0);
        if (element != null) {
            NodeList childElements = element.getElementsByTagName(subElementName);
            for (int i = 0; i < childElements.getLength(); i++) {
                String values = childElements.item(i).getTextContent();
                elementCollection.add(values);
            }
        }
    }

    public LinkedList<GameAction> getGameActions() {
        return this.gameActions;
    }

}
