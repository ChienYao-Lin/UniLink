package rmit.s3790516.unilink.model.post;

import rmit.s3790516.unilink.model.exception.*;
import rmit.s3790516.unilink.model.reply.Reply;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

public class Job extends Post {

    private float proposedPrice;
    private float lowestOffer;

    // create a new job
    public Job(String p_ID, String p_title, String desc, String c_ID, float pPrice) throws TextLengthException, BlankInputException, NumberNonPositiveException, SemicolonException {
        super(p_ID, p_title, desc, c_ID);
        if (isProposedPriceValid(pPrice)) {
            this.proposedPrice = pPrice;
        }
        this.lowestOffer = -1;
    }

    // create a job from database
    public Job(ResultSet resultSet, ArrayList<Reply> repliesArraylist) throws SQLException {
        super(resultSet, repliesArraylist);
        this.proposedPrice = resultSet.getFloat("PROPOSEDPRICE");
        this.lowestOffer = resultSet.getFloat("LOWESTOFFER");
    }

    // create a job from text file
    public Job(String p_ID, String[] data, ArrayList<Reply> repliesArraylist) throws TextLengthException, SemicolonException, StatusTypeException, BlankInputException, ArrayIndexOutOfBoundsException, NumberNonPositiveException, StudentNumberException, NumberFormatException, ReplyFailException {
        super(p_ID, data, repliesArraylist);
        float pPrice = Float.parseFloat(data[9]);
        float lowestP = Float.parseFloat(data[10]);
        if (isProposedPriceValid(pPrice)) {
            this.proposedPrice = pPrice;
        }
        if (isDecreasing(repliesArraylist) && isLowestOfferValid(lowestP)) {
            this.lowestOffer = lowestP;
        }
    }

    public String getProposedPrice() {
        return String.valueOf(proposedPrice);
    }

    public void setProposedPrice(float pPrice) throws NumberNonPositiveException {
        if (pPrice <= 0) {
            throw new NumberNonPositiveException("Save failed: proposed price should be greater than 0.");
        }
        this.proposedPrice = pPrice;
        update();
    }

    public String getLowestOffer() {
        if (lowestOffer == -1) {
            return "NO OFFER";
        } else {
            return String.valueOf(lowestOffer);
        }
    }

    @Override
    protected String getPostDBDetails() {
        return super.getPostDBDetails() + ", " + proposedPrice + ", " + lowestOffer;
    }

    @Override
    public String getPostUpdateDBDetails() {
        return super.getPostUpdateDBDetails() + ", ProposedPrice = " + proposedPrice + ", LowestOffer = " + lowestOffer;
    }

    @Override
    public String getPostExportDetails() {
        return super.getPostExportDetails() + ";" + proposedPrice + ";" + lowestOffer;
    }

    @Override
    public String handleReply(Reply reply) throws ReplyFailException {
        float reVal = reply.getValue();
        // Check if the reply value is valid
        if (reVal < lowestOffer || (lowestOffer == -1 && reVal <= proposedPrice)) {
            lowestOffer = reVal;
            replies.add(reply);
            update();
            return "Your offer has been submitted!";
            // If invalid
        } else {
            // value > proposed value
            if (lowestOffer == -1) {
                throw new ReplyFailException("Offer not accepted!\n" +
                        "Please ensure your offer is less than or equal to " + proposedPrice);
                // value >= lowest value
            } else {
                throw new ReplyFailException("Offer not accepted!\n" +
                        "Please ensure your offer is less than " + lowestOffer);
            }
        }
    }

    @Override
    public String getReplyInfo() {
        // initialisation
        String infoString = "";

        infoString += "Name: " + getTitle() + "\n";
        infoString += "Proposed price: " + proposedPrice + "\n";
        if (lowestOffer == -1) {
            infoString += "Lowest offer: NO OFFER" + "\n";
        } else {
            infoString += "Lowest offer: " + lowestOffer + "\n";
        }

        return infoString;
    }

    @Override
    public ArrayList<Reply> getReplies() {
        ArrayList<Reply> sortedReplies = (ArrayList<Reply>) replies.clone();
        Collections.sort(sortedReplies);
        return sortedReplies;
    }

    @Override
    //save this job to database
    public void save(Statement stmt) throws SQLException {
        if (isLoad()) {
            if (isUpdate()) {
                String query = "UPDATE job" +
                        " SET " + getPostUpdateDBDetails() +
                        "WHERE ID LIKE '" + getID() + "'";
                stmt.executeUpdate(query);
            }
        } else {
            String query = "INSERT INTO job" +
                    " VALUES (" + getPostDBDetails() + ")";
            stmt.executeUpdate(query);
        }
    }

    private boolean isProposedPriceValid(float pPrice) throws NumberNonPositiveException {
        if (pPrice <= 0) {
            throw new NumberNonPositiveException("Proposed price should be greater than 0.");
        } else {
            return true;
        }
    }

    private boolean isLowestOfferValid(float lowestP) throws NumberNonPositiveException {
        if (lowestP == replies.get(getReplyCount() - 1).getValue() || lowestP == -1) {
            return true;
        } else {
            throw new NumberNonPositiveException("Lowest offer is not valid.");
        }
    }

    private boolean isDecreasing(ArrayList<Reply> repliesArraylist) throws ReplyFailException {
        for (int i = 1; i < repliesArraylist.size(); i++) {
            if (repliesArraylist.get(i - 1).compareTo(repliesArraylist.get(i)) <= 0)
                throw new ReplyFailException("Replies in a job post should be decreasing.");
        }
        return true;
    }
}
