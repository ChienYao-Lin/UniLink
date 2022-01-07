package rmit.s3790516.unilink;

import rmit.s3790516.unilink.controller.LoginViewController;
import rmit.s3790516.unilink.controller.MainWindowViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rmit.s3790516.unilink.model.UnilinkSystem;

import java.io.IOException;
import java.sql.SQLException;

public class UniLinkApplication extends Application {
    private UnilinkSystem unilinkSystem;

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void init() throws SQLException, ClassNotFoundException {
        unilinkSystem = new UnilinkSystem("UniLinkDB");
        unilinkSystem.loadData();
        MainWindowViewController.setUniLinkSys(unilinkSystem);
        LoginViewController.setUniLinkSys(unilinkSystem);
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(UniLinkApplication.class.getResource("login_view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 420, 175);
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @Override
    public void stop() throws SQLException, ClassNotFoundException {
        unilinkSystem.saveData();
    }
}
