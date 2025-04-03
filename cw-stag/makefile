# Define the Maven wrapper and the Maven exec goal
MVNW = ./mvnw
EXEC = exec:java@strange

# List all Java classes you want to run (without .java)
CLASSES = GameServer GameController LoadEntityFile LoadActionFile GameAction GameArtefactFactory GameArtefacts GameCharacterFactory GameCharacters GameCommands GameCustomCommands GameEntity GameFurniture GameFurnitureFactory GameItemFactory GameLocations GamePlayers 

# Default target: run all classes
all: run-all

# Run all classes one after the other
run-all: $(addprefix run-, $(CLASSES))
	@echo "All classes executed successfully."

# Pattern rule to run a given class.
# $* represents the class name (e.g. GameServer)
run-%:
	@echo "Running $*..."
	$(MVNW) $(EXEC) -Dexec.args=src/main/java/edu/uob/$*.java
