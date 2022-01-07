package rmit.s3790516.unilink;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import rmit.s3790516.unilink.model.UnilinkSystem;

import java.io.IOException;
import java.sql.SQLException;

public class LoginViewController {
    private static UnilinkSystem unilinkSystem;
    @FXML
    private Label infoLabel;
    @FXML
    private TextField userNameTextField;
    @FXML
    private Button loginButton, quitButton;

    public static void setUniLinkSys(UnilinkSystem uni) {
        unilinkSystem = uni;
    }

    @FXML
    private void initialize() throws SQLException, ClassNotFoundException {
        infoLabel.setText("");
    }

    @FXML
    private void loginButtonHandler(ActionEvent actionEvent) {
        if (userNameTextField.getText().equals("")) {
            infoLabel.setText("Please Enter Your User Name!");
        } else if (!isIDValid(userNameTextField.getText())) {
            infoLabel.setText("Student id should begin with the character 's' followed by a number.");
        } else {
            FXMLLoader mainFxmlLoader = new FXMLLoader(getClass().getResource("main_window_view.fxml"));
            MainWindowViewController.setUserName(userNameTextField.getText());
            try {
                Parent root = mainFxmlLoader.load();
                Stage stage = new Stage();
                stage.setTitle("UniLink System");
                stage.setScene(new Scene(root, 950, 500));
                stage.show();
                ((Stage) loginButton.getScene().getWindow()).close();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Information");
                alert.setContentText("Graphical User Interface open failed.");
                alert.show();
            }
        }
    }

    @FXML
    private void quitButtonHandler(ActionEvent actionEvent) {
        ((Stage) quitButton.getScene().getWindow()).close();
    }

    @FXML
    public void enterEventHandler(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            loginButtonHandler(null);
        }
    }

    private boolean isIDValid(String ID) {
        return ID.matches("s\\d+");
    }
}
