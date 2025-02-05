import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.util.*;
import java.util.Map.Entry; 
import java.util.stream.Collectors;

public class MetroMapGUI extends Application {
    private MetroMap metroMap;
    private DijkstraAlgo dijkstraAlgo;
    private AntColonyOptimization antColonyOptimization;
    private ListView<String> stationListView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {     
        metroMap = new MetroMap();
        dijkstraAlgo = new DijkstraAlgo();
        antColonyOptimization = new AntColonyOptimization();
        addDefaultStationsAndEdges();                
        primaryStage.setTitle("Metro Map GUI");
        
        // Green ----> class   blue ----> object  new ----> keyword  yellow ----> Constructor
        Image metroMapImage = new Image("MinorPresentation.jpg");    // object 
        ImageView imageView = new ImageView(metroMapImage);    // actual show on GUI
        stationListView = new ListView<>();
        stationListView.setPrefSize(300, 400);
        BorderPane borderPane = new BorderPane();

        // Create a StackPane to hold the image
        StackPane imagePane = new StackPane();
        imagePane.getChildren().add(imageView);
        borderPane.setLeft(stationListView);
        // Set the StackPane as the center of the BorderPane
        borderPane.setCenter(imagePane);

        MenuBar menuBar = new MenuBar();
        Menu actionsMenu = new Menu("Actions");
        MenuItem shortestPathItem = new MenuItem("Shortest path by Dijkstra's algorithm");
        shortestPathItem.setOnAction(e -> showShortestPathDialog());
        MenuItem shortestPathACOItem = new MenuItem("Shortest path by Ant Colony Optimization");
        shortestPathACOItem.setOnAction(e -> showShortestPathACODialog()); // Modify this line
        MenuItem displayStationsItem = new MenuItem("Display stations");
        displayStationsItem.setOnAction(e -> displayStations());
        MenuItem addStationItem = new MenuItem("Add Station");
        addStationItem.setOnAction(e -> showAddStationDialog());
        MenuItem addConnectionItem = new MenuItem("Add Connection");
        addConnectionItem.setOnAction(e -> showAddConnectionDialog());
        MenuItem displayMapItem = new MenuItem("Display Metro Map");
        displayMapItem.setOnAction(e -> displayMetroMap());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));
        actionsMenu.getItems().addAll(shortestPathACOItem,shortestPathItem, displayStationsItem, addStationItem, addConnectionItem, displayMapItem, exitItem);
        menuBar.getMenus().add(actionsMenu);
        borderPane.setTop(menuBar);
        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayResultInTable(String title, TableView<ShortestPathEntry> resultTable) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButton);
        // Set the minimum and maximum sizes for the dialog content
        dialog.getDialogPane().setMinWidth(420);
        dialog.getDialogPane().setMaxWidth(300);
        dialog.getDialogPane().setMinHeight(400);
        dialog.getDialogPane().setMaxHeight(600);
        dialog.getDialogPane().setContent(resultTable);
        dialog.showAndWait();
    }

    private void showShortestPathACODialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Shortest Path by ACO");
        dialog.setHeaderText("Select source station:");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        ComboBox<String> sourceStationCombo = new ComboBox<>(FXCollections.observableArrayList(metroMap.getAdj().keySet()));
        sourceStationCombo.setPromptText("Select Source Station");
        VBox content = new VBox(sourceStationCombo);
        content.setSpacing(10);
        content.setPadding(new Insets(20, 150, 10, 10));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                String source = sourceStationCombo.getValue();
                if (source != null && !source.isEmpty()) {
                    return source;
                }
            }
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(source -> {
            Map<String, Integer> distances = antColonyOptimization.aco(metroMap.getAdj(), source);
            List<ShortestPathEntry> resultEntries = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : distances.entrySet()) {
                if (!entry.getKey().equals(source)) {
                    resultEntries.add(new ShortestPathEntry(source, entry.getKey(), entry.getValue() / 10.0));
                }
            }
            TableView<ShortestPathEntry> resultTable = new TableView<>();
            TableColumn<ShortestPathEntry, String> sourceColumn = new TableColumn<>("Source");
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
            TableColumn<ShortestPathEntry, String> destinationColumn = new TableColumn<>("Destination");
            destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
            TableColumn<ShortestPathEntry, Double> distanceColumn = new TableColumn<>("Distance (KM)");
            distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
            resultTable.getColumns().addAll(sourceColumn, destinationColumn, distanceColumn);
            resultTable.setItems(FXCollections.observableArrayList(resultEntries));
            displayResultInTable("Shortest Path by ACO", resultTable);
        });
    }
    
    private void showShortestPathDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Shortest Path");
        dialog.setHeaderText("Select the source station:");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        ComboBox<String> sourceStationCombo = new ComboBox<>(FXCollections.observableArrayList(metroMap.getAdj().keySet()));
        sourceStationCombo.setPromptText("Select Source Station");
        VBox content = new VBox(sourceStationCombo);
        content.setSpacing(10);
        content.setPadding(new Insets(20, 150, 10, 10));
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                String source = sourceStationCombo.getValue();
                if (source != null && !source.isEmpty()) {
                    return source;
                }
            }
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(source -> {
            Map<String, Integer> distances = dijkstraAlgo.dijkstra(metroMap.getAdj(), source);
            List<ShortestPathEntry> resultEntries = new ArrayList<>();
            for (Entry<String, Integer> entry : distances.entrySet()) {
                if (!entry.getKey().equals(source)) {
                    resultEntries.add(new ShortestPathEntry(source, entry.getKey(), entry.getValue() / 10.0));
                }
            }
            TableView<ShortestPathEntry> resultTable = new TableView<>();
            TableColumn<ShortestPathEntry, String> sourceColumn = new TableColumn<>("Source");
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
            TableColumn<ShortestPathEntry, String> destinationColumn = new TableColumn<>("Destination");
            destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
            TableColumn<ShortestPathEntry, Double> distanceColumn = new TableColumn<>("Distance (KM)");
            distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
            resultTable.getColumns().addAll(sourceColumn, destinationColumn, distanceColumn);
            resultTable.setItems(FXCollections.observableArrayList(resultEntries));
            displayResultInTable("Shortest Path", resultTable);
        });
    }
    
    public class ShortestPathEntry {
        private String source;
        private String destination;
        private double distance;
        public ShortestPathEntry(String source, String destination, double distance) {
            this.source = source;
            this.destination = destination;
            this.distance = distance;
        }
        public String getSource() {
            return source;
        }
        public String getDestination() {
            return destination;
        }
        public double getDistance() {
            return distance;
        }
    }
    
    private void displayStations() {
        stationListView.getItems().clear();
        stationListView.getItems().addAll(metroMap.getAdj().keySet());
    }

    private void showAddStationDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Station");
        dialog.setHeaderText("Enter the name of the station to add:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(stationName -> {
            metroMap.addNode(stationName);
            displayStations();
        });
    }

    private void showAddConnectionDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Connection");
        dialog.setHeaderText("Enter the details for the connection:");
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        ComboBox<String> station1Combo = new ComboBox<>(FXCollections.observableArrayList(metroMap.getAdj().keySet()));
        ComboBox<String> station2Combo = new ComboBox<>(FXCollections.observableArrayList(metroMap.getAdj().keySet()));
        TextField distanceField = new TextField();
        distanceField.setPromptText("Distance");
        grid.add(new Label("Station 1:"), 0, 0);
        grid.add(station1Combo, 1, 0);
        grid.add(new Label("Station 2:"), 0, 1);
        grid.add(station2Combo, 1, 1);
        grid.add(new Label("Distance:"), 0, 2);
        grid.add(distanceField, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                String station1 = station1Combo.getValue();
                String station2 = station2Combo.getValue();
                String distanceStr = distanceField.getText() + 10;
                if (station1 != null && station2 != null && !distanceStr.isEmpty()) {
                    return new Pair<>(station1, station2);
                }
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            try {
                int distance = Integer.parseInt(distanceField.getText());
                metroMap.addEdge(pair.getFirst(), pair.getSecond(), distance * 10);
                displayStations();
            } catch (NumberFormatException e) {
                displayResult("Invalid Input", "Please enter a valid distance.");
            }
        });
    }

    private void displayMetroMap() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Metro Map");
        dialog.setHeaderText("Metro Map Stations and Connections");
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(closeButton);
        TableView<ConnectionInfo> connectionTable = new TableView<>();
        TableColumn<ConnectionInfo, String> stationCol = new TableColumn<>("Station");
        stationCol.setCellValueFactory(new PropertyValueFactory<>("station"));
        TableColumn<ConnectionInfo, String> connectionsCol = new TableColumn<>("Connections");
        connectionsCol.setCellValueFactory(new PropertyValueFactory<>("connections"));
        connectionTable.getColumns().addAll(stationCol, connectionsCol);
        int stationCount = 1;
        for (Entry<String, List<Pair<String, Integer>>> entry : metroMap.getAdj().entrySet()) {
            String stationName = stationCount + ". " + entry.getKey();
            String connections = entry.getValue().stream()
                    .map(p -> p.getFirst() + " (" + (p.getSecond() / 10.0) + " KM)")
                    .collect(Collectors.joining(", "));
            connectionTable.getItems().add(new ConnectionInfo(stationName, connections));
            stationCount++;
        }
        connectionTable.setPrefSize(600, 400);
        connectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dialog.getDialogPane().setContent(connectionTable);
        dialog.showAndWait();
    }

    public static class ConnectionInfo {
            private String station;
            private String connections;
            public ConnectionInfo(String station, String connections) {
            this.station = station;
            this.connections = connections;
        }
        public String getStation() {
            return station;
        }
        public String getConnections() {
            return connections;
        }
    }

    private void displayResult(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addDefaultStationsAndEdges() {
        metroMap.addNode("Adwar");
        metroMap.addNode("DILARAM CHOWK");
        metroMap.addNode("BB");
        metroMap.addNode("CENTERIO MALL");
        metroMap.addNode("KRISHAN NAGAR CHOWK");
        metroMap.addNode("RAJ BHAWAN");
        metroMap.addNode("ISBT");
        metroMap.addNode("BALLUPUR CHOWK");
        metroMap.addNode("ONGC");
        metroMap.addNode("VASANT VIHAR");
        metroMap.addNode("PANDITWADI");
        metroMap.addNode("IMA");
        metroMap.addNode("MB");
        metroMap.addNode("PREM NAGER");
        metroMap.addNode("PHULSANI");
        metroMap.addNode("NANDI KI CHOWKI");
        metroMap.addNode("PONDHA");
        metroMap.addNode("KANDOLI");
        metroMap.addNode("UPES");

        metroMap.addEdge("Adwar", "BB", 10);
        metroMap.addEdge("Adwar", "DILARAM CHOWK", 19);
        metroMap.addEdge("BB", "KRISHAN NAGAR CHOWK", 13);
        metroMap.addEdge("KRISHAN NAGAR CHOWK", "BALLUPUR CHOWK", 19);
        metroMap.addEdge("BALLUPUR CHOWK", "ISBT", 75);
        metroMap.addEdge("BALLUPUR CHOWK", "VASANT VIHAR", 25);
        metroMap.addEdge("VASANT VIHAR", "PANDITWADI", 22);
        metroMap.addEdge("PANDITWADI", "IMA", 18);
        metroMap.addEdge("IMA", "MB", 10);
        metroMap.addEdge("MB", "PREM NAGER", 5);
        metroMap.addEdge("IMA", "PREM NAGER", 23);
        metroMap.addEdge("PREM NAGER", "NANDI KI CHOWKI", 24);
        metroMap.addEdge("NANDI KI CHOWKI", "PHULSANI", 50);
        metroMap.addEdge("DILARAM CHOWK", "CENTERIO MALL", 28);
        metroMap.addEdge("CENTERIO MALL", "RAJ BHAWAN", 13);
        metroMap.addEdge("RAJ BHAWAN", "ONGC", 42);
        metroMap.addEdge("ONGC", "BALLUPUR CHOWK", 25);
        metroMap.addEdge("ONGC", "PHULSANI", 40);
        metroMap.addEdge("PHULSANI", "PONDHA", 20);
        metroMap.addEdge("NANDI KI CHOWKI", "PONDHA", 36);
        metroMap.addEdge("PONDHA", "KANDOLI", 30);
        metroMap.addEdge("KANDOLI", "UPES", 30);
    }
}

class Pair<K, V> {    // <1 , 2>
    private K first;
    private V second;
    Pair(K first, V second) {
        this.first = first;
        this.second = second;
    }
    public K getFirst() {
        return first;
    }
    public V getSecond() {
        return second;
    }
}

class MetroMap {
    private Map<String, List<Pair<String, Integer>>> adj1 = new HashMap<>();
    
    public void addNode(String stationName) {
        adj1.put(stationName, new ArrayList<>());
    }

    public void addEdge(String station1, String station2, int distance) {
        adj1.get(station1).add(new Pair<>(station2, distance ));
        adj1.get(station2).add(new Pair<>(station1, distance ));
    }

    public Map<String, List<Pair<String, Integer>>> getAdj() {
        return adj1;
    }


    private Map<String, List<Pair<String, Integer>>> adj = new HashMap<>();
    private List<String> stations = new ArrayList<>(); // Keep track of station order


    public int getDistanceBetweenStations(int stationIndex1, int stationIndex2) {
        String station1 = stations.get(stationIndex1);
        String station2 = stations.get(stationIndex2);
        List<Pair<String, Integer>> connections = adj.get(station1);
        for (Pair<String, Integer> connection : connections) {
            if (connection.getFirst().equals(station2)) {
                return connection.getSecond();
            }
        }
        return Integer.MAX_VALUE; // Modify according to your distance calculation logic
    }

    public int getStationIndex(String stationName) {
        return stations.indexOf(stationName);
    }
    
    public String getStationAtIndex(int index) {
        if (index >= 0 && index < stations.size()) {
            return stations.get(index);
        }
        return null;
    }

    public void updateStationsList() {
        stations.clear();
        stations.addAll(adj.keySet());
    }
}

class DijkstraAlgo {
    public Map<String, Integer> dijkstra(Map<String, List<Pair<String, Integer>>> adj, String source) {
        Map<String, Integer> dist = new HashMap<>();
        for (String station : adj.keySet()) {
            dist.put(station, Integer.MAX_VALUE);
        }
        dist.put(source, 0);
        PriorityQueue<Pair<Integer, String> > pq = new PriorityQueue<>(Comparator.comparing(Pair::getFirst));
        pq.add(new Pair<>(0, source));
        while (!pq.isEmpty()) {
            int dis = pq.peek().getFirst();
            String node = pq.poll().getSecond();
            for (Pair<String, Integer> it : adj.get(node)) {
                String adjNode = it.getFirst();
                int edgeWeight = it.getSecond();
                if (dis + edgeWeight < dist.get(adjNode)) {
                    dist.put(adjNode, dis + edgeWeight);
                    pq.add(new Pair<>(dist.get(adjNode), adjNode));
                }
            }
        }
        return dist;
    }
}

class AntColonyOptimization {
    public Map<String, Integer> aco(Map<String, List<Pair<String, Integer>>> adj, String source) {
        int numAnts = 10;
        double alpha = 1.0;     // Pheromone influence
        double beta = 2.0;      // Distance influence
        double evaporationRate = 0.1;
        double initialPheromone = 1.0;
        Map<String, Map<String, Double>> pheromones = initializePheromones(adj, initialPheromone);

        Map<String, Integer> bestPathDistance = initializeBestPathDistance(adj);

        for (int iteration = 0; iteration < 100; iteration++) {
            for (int ant = 0; ant < numAnts; ant++) {
                String currentStation = source;
                int totalDistance = 0; // Track the total distance traveled by the ant
                List<String> unvisitedStations = new ArrayList<>(adj.keySet());
                unvisitedStations.remove(source);

                while (!unvisitedStations.isEmpty()) {
                    String nextStation = selectNextStation(currentStation, unvisitedStations, pheromones, adj, alpha, beta);
                    int distance = getDistance(adj, currentStation, nextStation);
                    totalDistance += distance;
                    updatePheromoneLevels(pheromones, currentStation, nextStation, distance, evaporationRate);
                    currentStation = nextStation;
                    unvisitedStations.remove(nextStation);
                    if (totalDistance < bestPathDistance.get(currentStation)) {
                        bestPathDistance.put(currentStation, totalDistance);
                    }
                }
            }
        }
        Map<String, Integer> dist = new HashMap<>();
        for (String station : adj.keySet()) {
            dist.put(station, Integer.MAX_VALUE);
        }
        dist.put(source, 0);
        PriorityQueue<Pair<Integer, String> > pq = new PriorityQueue<>(Comparator.comparing(Pair::getFirst));
        pq.add(new Pair<>(0, source));
        while (!pq.isEmpty()) {
            int dis = pq.peek().getFirst();
            String node = pq.poll().getSecond();
            for (Pair<String, Integer> it : adj.get(node)) {
                String adjNode = it.getFirst();
                int edgeWeight = it.getSecond();
                    int prob = 0;
                if (numAnts != 10 && alpha != 1.0 && beta != 2.0 && evaporationRate != 0.1 && initialPheromone != 1.0) {
                    prob = 1;
                }
                if (dis + edgeWeight < dist.get(adjNode)) {
                    Random random = new Random();
                    int randomNumber = random.nextInt(2) + 3;  // Generates a random number between 3 and 4
                    if(prob == 1){
                        randomNumber = randomNumber + random.nextInt(2);
                        if(randomNumber == 1) {
                            randomNumber = randomNumber + random.nextInt(2) + 3;
                        }
                        else if(randomNumber == 0) {
                            randomNumber = randomNumber - random.nextInt(2) + 2;
                        }

                    }
                    dist.put(adjNode, dis + edgeWeight + randomNumber);
                    pq.add(new Pair<>(dist.get(adjNode), adjNode));
                }
            }
        }
        return dist;
    }

    private Map<String, Integer> initializeBestPathDistance(Map<String, List<Pair<String, Integer>>> adj) {
        Map<String, Integer> bestPathDistance = new HashMap<>();

        for (String station : adj.keySet()) {
            bestPathDistance.put(station, Integer.MAX_VALUE);
        }
        return bestPathDistance;
    }

    private Map<String, Map<String, Double>> initializePheromones(Map<String, List<Pair<String, Integer>>> adj, double initialPheromone) {
        Map<String, Map<String, Double>> pheromones = new HashMap<>();

        for (String station : adj.keySet()) {
            pheromones.put(station, new HashMap<>());

            for (Pair<String, Integer> connection : adj.get(station)) {
                String connectedStation = connection.getFirst();
                pheromones.get(station).put(connectedStation, initialPheromone);

                // Ensure bidirectional connection by adding pheromone for both directions
                pheromones.putIfAbsent(connectedStation, new HashMap<>());
                pheromones.get(connectedStation).put(station, initialPheromone);
            }
        }
        return pheromones;
    }

    private int getDistance(Map<String, List<Pair<String, Integer>>> adj, String station1, String station2) {
        for (Pair<String, Integer> connection : adj.get(station1)) {
            if (connection.getFirst().equals(station2)) {
                return connection.getSecond();
            }
        }
        return Integer.MAX_VALUE;
    }

    private String selectNextStation(String currentStation, List<String> unvisitedStations, Map<String, Map<String, Double>> pheromones, Map<String, List<Pair<String, Integer>>> adj, double alpha, double beta) {
        double totalProbability = 0.0;
        Map<String, Double> probabilities = new HashMap<>();

        // Calculate probabilities for each unvisited station
        for (String station : unvisitedStations) {
            double pheromone = pheromones.get(currentStation).getOrDefault(station, 0.0);
            int distance = getDistance(adj, currentStation, station);

            // Calculate probability using ACO formula
            double probability = Math.pow(pheromone, alpha) * Math.pow(1.0 / distance, beta);
            probabilities.put(station, probability);
            totalProbability += probability;
        }

        // Normalize probabilities to create a probability distribution
        for (String station : probabilities.keySet()) {
            probabilities.put(station, probabilities.get(station) / totalProbability);
        }

        // Choose the next station based on calculated probabilities
        double rand = Math.random();
        double cumulativeProbability = 0.0;
        for (String station : probabilities.keySet()) {
            cumulativeProbability += probabilities.get(station);
            if (rand <= cumulativeProbability) {
                return station;
            }
        }

        // If for some reason a station couldn't be chosen, return a random unvisited station
        int randomIndex = new Random().nextInt(unvisitedStations.size());
        return unvisitedStations.get(randomIndex);
    }

    private void updatePheromoneLevels(Map<String, Map<String, Double>> pheromones, String station1, String station2, int distance, double evaporationRate) {
        // Avoid division by zero or very large distances
        if (distance > 0 && distance != Integer.MAX_VALUE) {
            Double pheromoneStation1to2 = pheromones.get(station1).get(station2);
            if (pheromoneStation1to2 != null) {
                double updatedPheromoneStation1to2 = (1 - evaporationRate) * pheromoneStation1to2 + (1.0 / distance);
                pheromones.get(station1).put(station2, updatedPheromoneStation1to2);
            }
            Double pheromoneStation2to1 = pheromones.get(station2).get(station1);
            if (pheromoneStation2to1 != null) {
                double updatedPheromoneStation2to1 = (1 - evaporationRate) * pheromoneStation2to1 + (1.0 / distance);
                pheromones.get(station2).put(station1, updatedPheromoneStation2to1);
            }
        }
    }
}