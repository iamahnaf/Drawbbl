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

public class ScoreboardController {

    @FXML
    private TableView<Score> scoreTable;
    @FXML
    private TableColumn<Score, Integer> scoreColumn;
    @FXML
    private TableColumn<Score, String> wordColumn; // NEW FXML BINDING
    @FXML
    private TableColumn<Score, String> dateColumn;

    @FXML
    public void initialize() {
        // Set up the columns to accept data from the Score class
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word")); // NEW
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadScores(GameContext.getLoggedInUsername());
    }


    private void loadScores(String username) {
        ObservableList<Score> scores = FXCollections.observableArrayList();
        try (BufferedReader br = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                // The line should now have 4 parts: user,score,word,date
                if (parts.length == 4 && parts[0].equalsIgnoreCase(username)) {
                    int scoreValue = Integer.parseInt(parts[1]);
                    String word = parts[2];
                    String date = parts[3];
                    scores.add(new Score(username, scoreValue, word, date));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read scores.txt: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing score value: " + e.getMessage());
        }

        scores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));

        scoreTable.setItems(scores);
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