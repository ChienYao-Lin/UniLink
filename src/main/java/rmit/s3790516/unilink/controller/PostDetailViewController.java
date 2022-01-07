package rmit.s3790516.unilink.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rmit.s3790516.unilink.model.post.*;
import  rmit.s3790516.unilink.model.reply.Reply;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.util.Optional;

public class PostDetailViewController {
    private final Post post;
    @FXML
    private ImageView postImageView;
    @FXML
    private Button backToMainButton, uploadImageButton, closePostButton, deletePostButton, saveButton;
    @FXML
    private Label titleLabel, postIDLabel, creatorLabel, statusLabel, updateTimeLabel;
    @FXML
    private Label detailLabel1, detailLabel2, detailLabel3;
    @FXML
    private TextField detailText2, detailText3, descriptionText, titleText;
    @FXML
    private TextArea detailText1, repliesDetailTextArea;
    @FXML
    private VBox postDetailsVBox;
    @FXML
    private TableView<Reply> replyDetailsTable;
    @FXML
    private TableColumn<Reply, String> stuNumberCol;
    @FXML
    private TableColumn<Reply, Float> priceCol;

    public PostDetailViewController(Post p) {
        this.post = p;
    }

    @FXML
    private void initialize() {
        setPostDetails();
        setPostImageView();
        setRepliesDetail();

        // set GUI controls to be disable for the post contains more than one reply
        if (post.getReplyCount() != 0) {
            titleText.setDisable(true);
            descriptionText.setDisable(true);
            detailText1.setDisable(true);
            detailText2.setDisable(true);
            detailText3.setDisable(true);
            saveButton.setDisable(true);
            uploadImageButton.setDisable(true);
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
    private void closePostButtonHandler(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Confirmation");
        alert.setContentText("The post will be closed and it is no longer possible to re-open it");
        Optional<ButtonType> result = alert.showAndWait();
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            post.closePost();
            closePostButton.setDisable(true);
        }
    }

    @FXML
    private void saveButtonHandler(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create post Information");
        try {
            post.setTitle(titleText.getText());
            post.setDescription(descriptionText.getText());
            if (post instanceof Event) {
                ((Event) post).setVenue(detailText1.getText());
                ((Event) post).setDate(detailText2.getText());
                ((Event) post).setCapacity(Integer.parseInt(detailText3.getText()));
            } else if (post instanceof Sale) {
                ((Sale) post).setAskingPrice(Float.parseFloat(detailText2.getText()));
                ((Sale) post).setMinRaise(Float.parseFloat(detailText3.getText()));
            } else {
                ((Job) post).setProposedPrice(Float.parseFloat(detailText2.getText()));
            }
            // Check image
            post.setHaveImage(renameImage());
            alert.setContentText("Save Success!");
        } catch (ParseException e) {
            alert.setContentText("Creation failed: The date format is incorrect. It must be in the format dd/mm/yyyy");
        } catch (NumberFormatException e) {
            alert.setContentText("Creation failed: The number input format is incorrect.");
        } catch (Exception e) {
            alert.setContentText(e.getMessage());
        } finally {
            alert.show();
        }
    }

    @FXML
    private void deletePostButtonHandler(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deletion Confirmation");
        alert.setContentText("The post will be deleted. Are you sure to delete it?");
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == yesButton)) {
            post.deletePost();
            FXMLLoader mainFxmlLoader = new FXMLLoader(getClass().getResource("/view/main_window_view.fxml"));
            try {
                Parent root = mainFxmlLoader.load();
                Stage stage = new Stage();
                stage.setTitle("UniLink System");
                stage.setScene(new Scene(root, 950, 500));
                stage.show();
                ((Stage) backToMainButton.getScene().getWindow()).close();
            } catch (IOException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Information");
                alert.setContentText("Graphical User Interface open failed.");
                alert.show();
            }
        }
    }

    @FXML
    private void uploadImageButtonHandler(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(postDetailsVBox.getScene().getWindow());
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
                Image saveImage = new Image(input, 170, 170, true, true);
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

    @FXML
    private void setPostDetails() {
        titleText.setText(post.getTitle());
        descriptionText.setText(post.getDesc());
        postIDLabel.setText("Post ID: " + post.getID());
        creatorLabel.setText("Creator: " + post.getCID());
        statusLabel.setText("Status: " + post.getStatus());
        updateTimeLabel.setText("Last Update: " + post.getTime());

        if (post instanceof Event) {
            detailLabel1.setText("Venue: ");
            detailText1.setText(((Event) post).getVenue());
            detailText1.setWrapText(true);
            detailLabel2.setText("Date: ");
            detailText2.setText(((Event) post).getDate());
            detailText2.setPrefWidth(170);
            detailLabel3.setText("Capacity: ");
            detailText3.setText(((Event) post).getCap());
        } else if (post instanceof Sale) {
            detailLabel1.setVisible(false);
            detailText1.setVisible(false);
            detailLabel2.setText("Asking Price: ");
            detailText2.setText(((Sale) post).getAskingPrice());
            detailLabel3.setText("Minimum Raise: ");
            detailText3.setText(((Sale) post).getMinRaise());
        } else {
            detailLabel1.setVisible(false);
            detailText1.setVisible(false);
            detailLabel2.setText("Proposed Price: ");
            detailText2.setText(((Job) post).getProposedPrice());
            detailLabel3.setVisible(false);
            detailText3.setVisible(false);
        }
    }

    @FXML
    private void setRepliesDetail() {
        if (post instanceof Event) {
            replyDetailsTable.setVisible(false);
            repliesDetailTextArea.setText(((Event) post).getReplyDetails());
        } else {
            repliesDetailTextArea.setVisible(false);
            ObservableList<Reply> replies = FXCollections.observableArrayList(post.getReplies());
            replyDetailsTable.setItems(replies);
            stuNumberCol.setCellValueFactory(
                    new PropertyValueFactory<Reply, String>("responder_ID"));
            priceCol.setCellValueFactory(
                    new PropertyValueFactory<Reply, Float>("value"));
        }
    }

    @FXML
    private void setPostImageView() {
        FileInputStream input = null;
        try {
            input = new FileInputStream("./images/" + post.getImageName());
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Image Error");
            alert.setContentText("Can not find the image for " + post.getID() + "\n" +
                    "The image will be set as default image.\n");
            alert.show();
            try {
                post.setHaveImage(false);
                input = new FileInputStream("./images/default.jpg");
            } catch (FileNotFoundException fileNotFoundException) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Image Error");
                alert.setContentText("./images/default.jpg missing");
                alert.show();
            }
        }
        Image image = new Image(input, 170, 170, true, true);
        postImageView.setImage(image);
        clipImage();
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
        File file2 = new File("./images/" + post.getID() + ".jpg");
        return (file1.renameTo(file2) | file2.exists());
    }
}
