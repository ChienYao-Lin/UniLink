package rmit.s3790516.unilink;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import rmit.s3790516.unilink.model.exception.ReplyFailException;
import rmit.s3790516.unilink.model.post.Event;
import rmit.s3790516.unilink.model.post.Job;
import rmit.s3790516.unilink.model.post.Post;
import rmit.s3790516.unilink.model.post.Sale;
import rmit.s3790516.unilink.model.reply.Reply;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public class PostListViewCellController extends ListCell<Post> {
    private static String userName;
    @FXML
    private Label IDLabel, creatorLabel, titleLabel, statusLabel, descLabel,
            detailLabel1, detailLabel2, detailLabel3, detailLabel4;
    @FXML
    private Button replyButton, moreDetailsButton;
    @FXML
    private FXMLLoader fxmlLoader;
    @FXML
    private HBox hBox;
    private Post post;
    private MainWindowViewController mainController;
    @FXML
    private ImageView postImageView;

    public PostListViewCellController(String uName, MainWindowViewController controller) {
        userName = uName;
        mainController = controller;
    }

    @Override
    protected void updateItem(Post p, boolean empty) {
        super.updateItem(p, empty);
        post = p;
        if (empty || post == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("post_list_view_cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Information");
                    alert.setContentText("Graphical User Interface open failed.");
                    alert.show();
                }
            }
            setPostImageView();
            setPostDetails();
            setBackground();
            setText(null);
            setGraphic(hBox);
        }
    }

    @FXML
    void moreDetailsButtonHandler(ActionEvent event) {
        FXMLLoader DetailsFxmlLoader = new FXMLLoader(getClass().getResource("/view/post_detail_view.fxml"));
        try {
            DetailsFxmlLoader.setController(new PostDetailViewController(post));
            Parent root = DetailsFxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Post Details");
            stage.setScene(new Scene(root, 825, 580));
            ((Stage) moreDetailsButton.getScene().getWindow()).close();
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Information");
            alert.setContentText("Graphical User Interface open failed.");
            alert.show();
        }
    }

    @FXML
    void replyButtonHandler(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reply Information");

        try {
            // check if the reply is valid
            Reply reply = replyQuery();
            if (reply != null) {
                alert.setContentText(post.handleReply(reply));
            }
        } catch (ReplyFailException e) {
            alert.setContentText(e.getMessage());
        } finally {
            alert.show();
            updateItem(post, false);
            MainWindowViewController.updateList();
            mainController.showListView();
        }
    }

    @FXML
    private void setPostDetails() {
        IDLabel.setText("ID: " + post.getID());
        creatorLabel.setText("Creator: " + post.getCID());
        titleLabel.setText("Title: " + post.getTitle());
        statusLabel.setText("Status: " + post.getStatus());
        descLabel.setText("Description: " + post.getDesc());
        descLabel.setWrapText(true);
        moreDetailsButton.setVisible(post.getCID().equals(userName));
        replyButton.setDisable(post.getStatus().equals("CLOSED"));
        if (post instanceof Event) {
            detailLabel1.setText("Venue: " + ((Event) post).getVenue());
            detailLabel2.setText("Date: " + ((Event) post).getDate());
            detailLabel3.setText("Attendee Count/ Capacity: \n");
            detailLabel4.setText(((Event) post).getAttCount() + "/" + ((Event) post).getCap());
        } else if (post instanceof Sale) {
            detailLabel1.setText("Highest Price: " + ((Sale) post).getHighestOffer());
            detailLabel2.setText("Minimum Raise: " + ((Sale) post).getMinRaise());
            detailLabel3.setText("Asking Price: " + ((Sale) post).getAskingPrice());
            detailLabel4.setText("");
            if (userName != post.getCID()) {
                detailLabel3.setVisible(false);
            }
        } else if (post instanceof Job) {
            detailLabel1.setText("Lowest Offer: " + ((Job) post).getLowestOffer());
            detailLabel2.setText("Proposed Price: " + ((Job) post).getProposedPrice());
            detailLabel3.setText("");
            detailLabel4.setText("");
        }
    }

    @FXML
    private void setBackground() {
        if (post instanceof Event) {
            hBox.setBackground(new Background(new BackgroundFill(Color.rgb(224, 255, 255), CornerRadii.EMPTY, Insets.EMPTY)));
        } else if (post instanceof Sale) {
            hBox.setBackground(new Background(new BackgroundFill(Color.rgb(255, 182, 193), CornerRadii.EMPTY, Insets.EMPTY)));
        } else if (post instanceof Job) {
            hBox.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 237), CornerRadii.EMPTY, Insets.EMPTY)));
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
        Image image = new Image(input, 120, 120, true, true);
        postImageView.setImage(image);
    }

    @FXML
    private Reply replyQuery() throws ReplyFailException {
        Reply reply = null;
        if (post instanceof Event) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reply");
            // Show reply info
            alert.setContentText("Would you want to join this event?\n" + post.getReplyInfo());
            ButtonType joinButton = new ButtonType("Join", ButtonBar.ButtonData.YES);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(joinButton, cancelButton);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == joinButton) {
                reply = new Reply(post.getID(), 1, userName);
            }
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Reply");
            // Show reply info
            dialog.setContentText("Please enter the price you want.\n" + post.getReplyInfo());
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    reply = new Reply(post.getID(), Float.parseFloat(result.get()), userName);
                } catch (NumberFormatException e) {
                    throw new ReplyFailException("Wrong input format!");
                }
            }
        }
        return reply;
    }
}
