package rmit.s3790516.unilink;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rmit.s3790516.unilink.model.post.Event;
import rmit.s3790516.unilink.model.post.Job;
import rmit.s3790516.unilink.model.post.Post;
import rmit.s3790516.unilink.model.post.Sale;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;


public class NewPostViewController {
    private final String cID;
    private final String pID;
    private final String postType;
    @FXML
    private ImageView postImageView;
    @FXML
    private Button backToMainButton, uploadImageButton;
    @FXML
    private Pane postColorPane;
    @FXML
    private Button createButton;
    @FXML
    private TextField titleText, detailText1, detailText2, detailText3;
    @FXML
    private TextArea descText;
    @FXML
    private Label detailLabel1, detailLabel2, detailLabel3, descCountLabel, titleCountLabel, detailText3CountLabel;

    public NewPostViewController(String creator, String postID, String pType) {
        this.cID = creator;
        this.pID = postID;
        this.postType = pType;
    }

    @FXML
    private void initialize() {
        titleCountLabel.textProperty().bind(Bindings.format("%02d/25", titleText.textProperty().length()));
        descCountLabel.textProperty().bind(Bindings.format("%02d/70", descText.textProperty().length()));
        setBackground();
        setDetailsContent();
    }

    @FXML
    private void createButtonHandler(ActionEvent event) {
        Post post;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create post Information");
        try {
            if (postType.equals("EVE")) {
                post = new Event(getpID(), titleText.getText(), descText.getText(), getcID(),
                        Integer.parseInt(detailText1.getText()), detailText2.getText(), detailText3.getText());
            } else if (postType.equals("SAL")) {
                post = new Sale(getpID(), titleText.getText(), descText.getText(), getcID(),
                        Float.parseFloat(detailText1.getText()), Float.parseFloat(detailText2.getText()));
            } else {
                post = new Job(getpID(), titleText.getText(), descText.getText(), getcID(),
                        Float.parseFloat(detailText1.getText()));
            }
            // Check if upload image
            post.setHaveImage(renameImage());
            // add post
            MainWindowViewController.addPost(post);

            FXMLLoader mainFxmlLoader = new FXMLLoader(getClass().getResource("/view/main_window_view.fxml"));

            Parent root = mainFxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("UniLink System");
            stage.setScene(new Scene(root, 950, 500));
            alert.setContentText("Success! Your post has been created with id " + getpID());
            stage.show();
            ((Stage) createButton.getScene().getWindow()).close();
        } catch (ParseException e) {
            alert.setContentText("Creation failed: The date format is incorrect. It must be in the format dd/mm/yyyy");
        } catch (IOException e) {
            alert.setContentText("Graphical User Interface open failed.");
        } catch (NumberFormatException e) {
            alert.setContentText("Creation failed: The number input format is incorrect.");
        } catch (Exception e) {
            alert.setContentText(e.getMessage());
        } finally {
            alert.show();
        }
    }

    @FXML
    private void backToMainButtonHandler(ActionEvent event) {
        FXMLLoader mainFxmlLoader = new FXMLLoader(getClass().getResource("/view/main_window_view.fxml"));
        try {
            // delete temp image file before back to main window
            File image = new File("./images/temp.jpg");
            if (image.exists()){
                image.delete();
            }

            Parent root = mainFxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("UniLink System");
            stage.setScene(new Scene(root, 950, 500));
            stage.show();
            ((Stage) backToMainButton.getScene().getWindow()).close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @FXML
    private void uploadImageButtonHandler(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(postImageView.getScene().getWindow());
        if (selectedFile != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Upload Information");
            try {
                InputStream input = new FileInputStream(selectedFile);
                BufferedImage image = ImageIO.read(input);
                OutputStream output = new FileOutputStream("./images/temp.jpg");

                // save the uploaded image in ./images as temp.jpg
                // it will rename as [PostID].jpg when pushing save button
                ImageIO.write(image, "jpg", output);
                input = new FileInputStream("./images/temp.jpg");
                Image saveImage = new Image(input, 120, 120, true, true);
                postImageView.setImage(saveImage);
                clipImage();
                alert.setContentText("Upload Successfully!!");
            } catch (FileNotFoundException e) {
                alert.setContentText("Upload failed: Image not found.");
            } catch (IOException e) {
                alert.setContentText("Upload failed: Write file in the local directory failed.");
            } finally {
                alert.show();
            }
        }
    }

    public String getcID() {
        return cID;
    }

    public String getpID() {
        return pID;
    }

    @FXML
    private void setDetailsContent() {
        if (postType.equals("EVE")) {
            detailLabel1.setText("Capacity");
            detailLabel2.setText("Date");
            detailText2.setPromptText("dd/mm/yyyy");
            detailLabel3.setText("Venue");
            detailText3.setPromptText("Venue (up to 40 characters)");
            detailText3CountLabel.textProperty().bind(Bindings.format("%02d/40", detailText3.textProperty().length()));
        } else if (postType.equals("SAL")) {
            detailLabel1.setText("Asking Price");
            detailLabel2.setText("Minimum Raise");
            detailLabel3.setVisible(false);
            detailText3.setVisible(false);
            detailText3CountLabel.setVisible(false);
        } else {
            detailLabel1.setText("Proposed Price");
            detailLabel2.setVisible(false);
            detailText2.setVisible(false);
            detailLabel3.setVisible(false);
            detailText3.setVisible(false);
            detailText3CountLabel.setVisible(false);
        }
    }

    @FXML
    private void setBackground() {
        if (postType.equals("EVE")) {
            postColorPane.setBackground(new Background(new BackgroundFill(Color.rgb(224, 255, 255), CornerRadii.EMPTY, Insets.EMPTY)));
        } else if (postType.equals("SAL")) {
            postColorPane.setBackground(new Background(new BackgroundFill(Color.rgb(255, 182, 193), CornerRadii.EMPTY, Insets.EMPTY)));
        } else {
            postColorPane.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 237), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    @FXML
    private void clipImage() {
        Rectangle clip = new Rectangle(postImageView.getFitWidth(), postImageView.getFitHeight());
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        postImageView.setClip(clip);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        WritableImage image = postImageView.snapshot(parameters, null);

        // remove the rounding clip so that our effect can show through.
        postImageView.setClip(null);
        // apply a shadow effect.
        postImageView.setEffect(new DropShadow(20, Color.BLACK));
        // store the rounded image in the imageView.
        postImageView.setImage(image);
    }

    private boolean renameImage() {
        File file1 = new File("./images/temp.jpg");
        File file2 = new File("./images/" + pID + ".jpg");
        return file1.renameTo(file2);
    }
}
