package rmit.s3790516.unilink.model.post;

import rmit.s3790516.unilink.model.exception.*;
import rmit.s3790516.unilink.model.reply.Reply;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

public class Sale extends Post {

    private float askingPrice;
    private float highestOffer;
    private float minRaise;

    // create a new sale
    public Sale(String p_ID, String p_title, String desc, String c_ID, float askP, float minR) throws TextLengthException, BlankInputException, NumberNonPositiveException, SemicolonException {
        super(p_ID, p_title, desc, c_ID);
        if (isAskingPriceValid(askP)) {
            this.askingPrice = askP;
        }
        if (isMinRaiseValid(minR)) {
            this.minRaise = minR;
        }
        this.highestOffer = 0;
    }

    // create a sale from database
    public Sale(ResultSet resultSet, ArrayList<Reply> repliesArraylist) throws SQLException {
        super(resultSet, repliesArraylist);
        this.askingPrice = resultSet.getFloat("ASKINGPRICE");
        this.highestOffer = resultSet.getFloat("HIGHESTOFFER");
        this.minRaise = resultSet.getFloat("MINRAISE");
    }

    // create a sale from text file
    public Sale(String p_ID, String[] data, ArrayList<Reply> repliesArraylist) throws TextLengthException, SemicolonException, StatusTypeException, BlankInputException, ArrayIndexOutOfBoundsException, NumberNonPositiveException, StudentNumberException, NumberFormatException, ReplyFailException {
        super(p_ID, data, repliesArraylist);
        float askP = Float.parseFloat(data[9]);
        float highestP = Float.parseFloat(data[10]);
        float minR = Float.parseFloat(data[11]);
        if (isAskingPriceValid(askP)) {
            this.askingPrice = askP;
        }
        if (isHighestOfferValid(highestP)) {
            this.highestOffer = highestP;
        }
        if (isIncreasing(repliesArraylist) && isMinRaiseValid(minR)) {
            this.minRaise = minR;
        }
    }

    public String getHighestOffer() {
        return String.valueOf(highestOffer);
    }

    public String getMinRaise() {
        return String.valueOf(minRaise);
    }

    public void setMinRaise(float minR) throws NumberNonPositiveException {
        if (isMinRaiseValid(minR)) {
            this.minRaise = minR;
        }
        update();
    }

    public String getAskingPrice() {
        return String.valueOf(askingPrice);
    }

    public void setAskingPrice(float askP) throws NumberNonPositiveException {
        if (isAskingPriceValid(askP)) {
            this.askingPrice = askP;
        }
        update();
    }

    @Override
    protected String getPostDBDetails() {
        return super.getPostDBDetails() + ", " + askingPrice + ", " + highestOffer + ", " + minRaise;
    }

    @Override
    public String getPostUpdateDBDetails() {
        return super.getPostUpdateDBDetails() + ", AskingPrice = " + askingPrice + ", HighestOffer = " + highestOffer + ", MinRaise = " + minRaise;
    }

    @Override
    public String getPostExportDetails() {
        return super.getPostExportDetails() + ";" + askingPrice + ";" + highestOffer + ";" + minRaise;
    }

    @Override
    public String handleReply(Reply reply) throws ReplyFailException {
        String replyInfo;
        // Check if the reply value is valid
        if (reply.getValue() >= highestOffer + minRaise) {
            highestOffer = reply.getValue();
            replies.add(reply);
            replyInfo = "Your offer has been submitted!\n";

            // If offer is greater than asking price, close the post
            if (highestOffer >= askingPrice) {
                closePost();
                replyInfo += "Congratulation! The " + getTitle() + " has been sold to you.\n" +
                        "Please contact the owner " + getCID() + " for more details.\n";
                // otherwise, keep opening
            } else {
                replyInfo += "However, your offer is below the asking price.\n" +
                        "The item is still on sale\n";
            }
            update();
            return replyInfo;
            // If invalid
        } else {
            throw new ReplyFailException("Offer not accepted!\n" +
                    "Please ensure your offer is higher than or equal to " + (highestOffer + minRaise));
        }
    }

    @Override
    public String getReplyInfo() {
        return "Name: " + getTitle() +
                "\nHighest price: " + highestOffer +
                "\nMinimum raise: " + minRaise + "\n";
    }

    @Override
    public ArrayList<Reply> getReplies() {
        ArrayList<Reply> sortedReplies = (ArrayList<Reply>) replies.clone();
        Collections.sort(sortedReplies, Collections.reverseOrder());
        return sortedReplies;
    }

    @Override
    // save this sale to database
    public void save(Statement stmt) throws SQLException {
        if (isLoad()) {
            if (isUpdate()) {
                String query = "UPDATE sale" +
                        " SET " + getPostUpdateDBDetails() +
                        " WHERE ID LIKE '" + getID() + "'";
                int result = stmt.executeUpdate(query);
            }
        } else {
            String query = "INSERT INTO sale" +
                    " VALUES (" + getPostDBDetails() + ")";
            stmt.executeUpdate(query);
        }
    }

    private boolean isAskingPriceValid(float askP) throws NumberNonPositiveException {
        if (askP <= 0) {
            throw new NumberNonPositiveException("Creation failed: asking price should be greater than 0.");
        } else {
            return true;
        }
    }

    private boolean isHighestOfferValid(float highestP) throws NumberNonPositiveException {
        if (highestP == replies.get(getReplyCount() - 1).getValue() || highestP == 0) {
            return true;
        } else {
            throw new NumberNonPositiveException("Highest Offer is not valid.");
        }
    }

    private boolean isMinRaiseValid(float minR) throws NumberNonPositiveException {
        if (minR <= 0) {
            throw new NumberNonPositiveException("Creation failed: minimum raise should be greater than 0.");
        } else {
            return true;
        }
    }

    private boolean isIncreasing(ArrayList<Reply> repliesArraylist) throws ReplyFailException {
        for (int i = 1; i < repliesArraylist.size(); i++) {
            if (repliesArraylist.get(i - 1).compareTo(repliesArraylist.get(i)) >= 0)
                throw new ReplyFailException("Replies in a sale post should be increasing.");
        }
        return true;
    }
}
