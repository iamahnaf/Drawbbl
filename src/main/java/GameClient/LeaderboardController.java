package GameClient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardController {

    // MODIFIED: The TableView now uses the new LeaderboardEntry class
    @FXML
    private TableView<LeaderboardEntry> leaderboardTable;
    @FXML
    private TableColumn<LeaderboardEntry, String> usernameColumn;
    @FXML
    private TableColumn<LeaderboardEntry, Integer> totalScoreColumn;

    @FXML
    public void initialize() {
        // Set up the columns to accept data from the LeaderboardEntry class
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        totalScoreColumn.setCellValueFactory(new PropertyValueFactory<>("totalScore"));

        loadAndAggregateScores();
    }

    // THIS IS THE NEW LOGIC
    private void loadAndAggregateScores() {
        // Use a Map to store the total score for each user.
        // Key: username (String), Value: total score (Integer)
        Map<String, Integer> scoreMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) { // user,score,word,date
                    String username = parts[0];
                    int scoreValue = Integer.parseInt(parts[1]);

                    // Add the score to the user's total.
                    // getOrDefault handles cases where the user isn't in the map yet.
                    scoreMap.put(username, scoreMap.getOrDefault(username, 0) + scoreValue);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read scores.txt: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing score value: " + e.getMessage());
        }

        // Convert the map entries into a list of LeaderboardEntry objects
        List<LeaderboardEntry> aggregatedScores = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
            aggregatedScores.add(new LeaderboardEntry(entry.getKey(), entry.getValue()));
        }

        // Sort the list from highest total score to lowest
        aggregatedScores.sort((e1, e2) -> Integer.compare(e2.getTotalScore(), e1.getTotalScore()));

        // Display the results in the table
        leaderboardTable.setItems(FXCollections.observableArrayList(aggregatedScores));
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ModeSelection.fxml"));
        Parent root = loader.load();
        ModeSelectionController controller = loader.getController();
        controller.initialize(GameContext.getLoggedInUsername());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}