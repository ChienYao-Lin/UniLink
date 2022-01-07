package rmit.s3790516.unilink;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rmit.s3790516.unilink.model.UnilinkSystem;
import rmit.s3790516.unilink.model.exception.PostTypeException;
import rmit.s3790516.unilink.model.post.Post;

import java.io.*;
import java.text.ParseException;
import java.util.function.Predicate;


public class MainWindowViewController {
    private static ObservableList<Post> postObservableList;
    private static UnilinkSystem unilinkSystem;
    private static String userName;
    @FXML
    private MenuBar mainWindowMenuBar;
    @FXML
    private Label currentUserLabel;
    @FXML
    private Button newEventPostButton, newSalePostButton, newJobPostButton, logOutButton;
    @FXML
    private ChoiceBox typeChoiceBox, statusChoiceBox, creatorChoiceBox;
    @FXML
    private ListView<Post> postListView;

    public static void setUniLinkSys(UnilinkSystem uni) {
        unilinkSystem = uni;
        updateList();
    }

    public static void updateList() {
        postObservableList = FXCollections.observableArrayList(unilinkSystem.getPosts()).sorted();
    }

    protected static void setUserName(String uName) {
        userName = uName;
    }

    protected static void addPost(Post p) {
        unilinkSystem.addPost(p);
        updateList();
    }

    @FXML
    private void initialize() {
        currentUserLabel.setText(userName);
        setChoiceBox();
        showListView();
    }

    @FXML
    private void setChoiceBox() {
        typeChoiceBox.getItems().addAll("All", "Event", "Sale", "Job");
        typeChoiceBox.setValue("All");
        statusChoiceBox.getItems().addAll("All", "OPEN", "CLOSED");
        statusChoiceBox.setValue("All");
        creatorChoiceBox.getItems().addAll("All", "My Post");
        creatorChoiceBox.setValue("All");
    }

    @FXML
    private FilteredList<Post> getFilteredList() {
        FilteredList<Post> filteredList = postObservableList.filtered(null);
        Observable[] dependencies = {typeChoiceBox.valueProperty(), statusChoiceBox.valueProperty(), creatorChoiceBox.valueProperty()};
        // create a binding between Predicate<Post> and Observable[] dependencies of ChoiceBox
        ObjectBinding<Predicate<Post>> binding = Bindings.createObjectBinding(() -> post -> {
            boolean typeTest, statusTest, creatorTest;
            // Test the post type
            if (typeChoiceBox.getValue().equals("All")) {
                typeTest = true;
            } else {
                typeTest = post.getClass().getSimpleName().equals(typeChoiceBox.getValue());
            }
            // Test the post status
            if (post.getStatus().equals("DELETED")) {
                statusTest = false;
            } else if (statusChoiceBox.getValue().equals("All")) {
                statusTest = true;
            } else {
                statusTest = post.getStatus().equals(statusChoiceBox.getValue());
            }
            // Test the post creator
            if (creatorChoiceBox.getValue().equals("All")) {
                creatorTest = true;
            } else {
                creatorTest = post.getCID().equals(currentUserLabel.getText());
            }
            return typeTest && statusTest && creatorTest;
        }, dependencies);
        filteredList.predicateProperty().bind(binding);
        return filteredList;
    }

    @FXML
    public void showListView() {
        FilteredList<Post> filteredList = getFilteredList();
        postListView.setItems(filteredList);
        postListView.setCellFactory(postListView -> new PostListViewCellController(userName, this));
    }

    @FXML
    private void logOutButtonHandler(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login_view.fxml"));
        try {
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(new Scene(root, 420, 175));
            stage.show();
            ((Stage) logOutButton.getScene().getWindow()).close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @FXML
    private void newEventPostButtonHandler(ActionEvent actionEvent) throws PostTypeException {
        FXMLLoader DetailsFxmlLoader = new FXMLLoader(getClass().getResource("/view/new_post_view.fxml"));
        String PID = unilinkSystem.getNewPostID("EVE");
        try {
            DetailsFxmlLoader.setController(new NewPostViewController(userName, PID, "EVE"));
            Parent root = DetailsFxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Event Details");
            stage.setScene(new Scene(root, 655, 440));
            ((Stage) newEventPostButton.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @FXML
    private void newSalePostButtonHandler(ActionEvent actionEvent) throws PostTypeException {
        FXMLLoader DetailsFxmlLoader = new FXMLLoader(getClass().getResource("/view/new_post_view.fxml"));
        String PID = unilinkSystem.getNewPostID("SAL");
        try {
            DetailsFxmlLoader.setController(new NewPostViewController(userName, PID, "SAL"));
            Parent root = DetailsFxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Sale Details");
            stage.setScene(new Scene(root, 655, 440));
            ((Stage) newEventPostButton.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @FXML
    private void newJobPostButtonHandler(ActionEvent actionEvent) throws PostTypeException {
        FXMLLoader DetailsFxmlLoader = new FXMLLoader(getClass().getResource("/view/new_post_view.fxml"));
        String PID = unilinkSystem.getNewPostID("JOB");
        try {
            DetailsFxmlLoader.setController(new NewPostViewController(userName, PID, "JOB"));
            Parent root = DetailsFxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Job Details");
            stage.setScene(new Scene(root, 655, 440));
            ((Stage) newEventPostButton.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @FXML
    private void importMenuItemHandler(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(mainWindowMenuBar.getScene().getWindow());

        if (selectedFile != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Import Information");
            int lineNumber = 0;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                String row;
                // Read post
                while ((row = reader.readLine()) != null) {
                    lineNumber++;
                    String[] data = row.split(";");
                    String PID = unilinkSystem.getNewPostID(data[0]);
                    // Read replies for the post
                    int repliesCount = Integer.parseInt(data[2]);
                    String rows = "";
                    for (int i = 0; i < repliesCount; i++) {
                        rows += reader.readLine() + "\n";
                    }
                    unilinkSystem.importPost(PID, data, repliesCount, rows);
                    lineNumber += repliesCount;
                }
                alert.setContentText("Import successfully.");
            } catch (FileNotFoundException e) {
                alert.setContentText("Import failed at line: Directory not found.");
            } catch (IOException e) {
                alert.setContentText("Import failed: I/O error happened.");
            } catch (ParseException e) {
                alert.setContentText("Line " + lineNumber + ": The date format is incorrect. It must be in the format dd/mm/yyyy");
            } catch (NumberFormatException e) {
                alert.setContentText("Line " + lineNumber + ": The number input format is incorrect.");
            } catch (Exception e) {
                alert.setContentText("Line " + lineNumber + ": " + e.getMessage());
            } finally {
                alert.show();
            }
            updateList();
            showListView();
        }
    }

    @FXML
    private void exportMenuItemHandler(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Set initial name
        fileChooser.setInitialFileName("export_data.txt");

        //Show save file dialog
        File file = fileChooser.showSaveDialog(mainWindowMenuBar.getScene().getWindow());

        if (file != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Information");
            try {
                unilinkSystem.exportData(file);
            } catch (FileNotFoundException e) {
                alert.setContentText("Export failed: Directory not found.");
                alert.show();
            }
        }
    }

    @FXML
    private void devInfoMenuItemHandler(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Developer Information");
        alert.setHeaderText(null);
        alert.setContentText("Developer: Chien-Yao Lin\n" +
                "Student Number: s3790516");
        alert.show();
    }

    @FXML
    private void quitMainItemHandler(ActionEvent event) {
        ((Stage) mainWindowMenuBar.getScene().getWindow()).close();
    }
}
