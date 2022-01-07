package rmit.s3790516.unilink.model.post;

import rmit.s3790516.unilink.model.exception.*;
import rmit.s3790516.unilink.model.reply.Reply;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class Post implements Comparable<Post> {
    protected ArrayList<Reply> replies;
    private String ID;
    private String title;
    private String description;
    private String creater_ID;
    private String status;
    private String updateTime;
    private boolean isUpdate;
    private boolean isLoad;
    private boolean haveImage;
    private int loadReplyCount;

    // Create a new post
    public Post(String p_ID, String p_title, String desc, String c_ID) throws TextLengthException, BlankInputException, SemicolonException {
        if (isTitleValid(p_title)) {
            this.title = p_title;
        }
        if (isDescValid(desc)) {
            this.description = desc;
        }

        this.ID = p_ID;
        this.creater_ID = c_ID;
        this.status = "OPEN";
        this.isUpdate = true;
        this.isLoad = false;
        this.replies = new ArrayList<Reply>();
        this.haveImage = false;

        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.updateTime = sdf.format(dt);
        this.loadReplyCount = 0;
    }

    // Create a post from database
    public Post(ResultSet resultSet, ArrayList<Reply> repliesArraylist) throws SQLException {
        this.ID = resultSet.getString("ID");
        this.title = resultSet.getString("TITLE");
        this.description = resultSet.getString("DESCRIPTION");
        this.creater_ID = resultSet.getString("CREATER_ID");
        this.status = resultSet.getString("STATUS");
        this.isUpdate = false;
        this.isLoad = true;
        this.updateTime = resultSet.getString("TIME");
        this.replies = repliesArraylist;
        this.loadReplyCount = repliesArraylist.size();
        this.haveImage = resultSet.getBoolean("HAVEIMAGE");
    }

    // Create a post from txt file
    public Post(String p_ID, String[] data, ArrayList<Reply> repliesArraylist) throws TextLengthException, BlankInputException, StatusTypeException, ArrayIndexOutOfBoundsException, SemicolonException, StudentNumberException {
        String p_title = data[3];
        String desc = data[4];
        String c_ID = data[5];
        String statusIn = data[6];
        String imageIn = data[8];

        if (isTitleValid(p_title)) {
            this.title = p_title;
        }
        if (isDescValid(desc)) {
            this.description = desc;
        }
        if (isStatusValid(statusIn)) {
            this.status = statusIn;
        }
        if (isIDValid(c_ID)) {
            this.creater_ID = c_ID;
        }
        if (!imageIn.equals("0")) {
            this.haveImage = renameImage();
        }

        this.ID = p_ID;
        this.isUpdate = true;
        this.isLoad = false;
        this.replies = repliesArraylist;

        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.updateTime = sdf.format(dt);
        this.loadReplyCount = 0;
    }

    protected String getPostDBDetails() {
        return "'" + ID + "', '" + title + "', '" + description + "', '" + creater_ID + "', '" + status + "', '" + updateTime + "', " + booleanToInt(haveImage);
    }

    protected String getPostUpdateDBDetails() {
        return "Title = '" + title + "', Description = '" + description + "', Status = '" + status + "', Time = '" + updateTime + "', HAVEIMAGE = " + booleanToInt(haveImage);
    }

    public String getPostExportDetails() {
        return ID.substring(0, 3) + ";" + ID.substring(3, 6) + ";" + getReplyCount() + ";" + title + ";" + description + ";" + creater_ID + ";" + status + ";" + updateTime + ";" + booleanToInt(haveImage);
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String p_title) throws BlankInputException, TextLengthException, SemicolonException {
        if (isTitleValid(p_title)) {
            this.title = p_title;
        }
        update();
    }

    public String getCID() {
        return creater_ID;
    }

    public String getID() {
        return ID;
    }

    public String getDesc() {
        return description;
    }

    public String getTime() {
        return updateTime;
    }

    public String getImageName() {
        if (haveImage) {
            return getID() + ".jpg";
        } else {
            return "default.jpg";
        }
    }

    public int getReplyCount() {
        return replies.size();
    }

    public void setDescription(String desc) throws BlankInputException, TextLengthException, SemicolonException {
        if (isDescValid(desc)) {
            this.description = desc;
        }
        update();
    }

    public void setHaveImage(boolean b) {
        haveImage = b;
        update();
    }

    public void closePost() {
        status = "CLOSED";
        update();
    }

    public void deletePost() {
        status = "DELETED";
        update();
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public boolean isLoad() {
        return isLoad;
    }

    protected void update() {
        isUpdate = true;
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        updateTime = sdf.format(dt);
    }

    public abstract String handleReply(Reply reply) throws ReplyFailException;

    public abstract String getReplyInfo();

    public abstract void save(Statement stmt) throws SQLException;

    public void saveReplies(Statement stmt) throws SQLException {
        // save replies to database
        for (int i = loadReplyCount; i < getReplyCount(); i++) {
            String query = "INSERT INTO reply" +
                    " VALUES (" + replies.get(i).getDBDetails() + ")";
            stmt.executeUpdate(query);
        }
    }

    public ArrayList<Reply> getReplies() {
        return replies;
    }

    public String getReplyExportDetails(int index) {
        return replies.get(index).getExportDetails();
    }

    @Override
    public int compareTo(Post post) {
        return -updateTime.compareTo(post.getTime());
    }

    private int booleanToInt(boolean b) {
        // true: 1, false: 0
        return b ? 1 : 0;
    }

    private boolean isIDValid(String stuID) throws StudentNumberException {
        // check student ID is valid
        if (stuID.matches("s\\d+")) {
            return true;
        } else {
            throw new StudentNumberException("Student id should begin with the character 's' followed by a number.");
        }
    }

    private boolean isStatusValid(String statusIn) throws StatusTypeException {
        // check status is valid
        if (statusIn.equals("OPEN") || statusIn.equals("CLOSED") || statusIn.equals("DELETED")) {
            return true;
        } else {
            throw new StatusTypeException("Post should be \"OPEN\", \"CLOSED\", or \"DELETED\".");
        }
    }

    private boolean isTitleValid(String p_title) throws SemicolonException, BlankInputException, TextLengthException {
        // Check semicolon
        if (p_title.contains(";")) {
            throw new SemicolonException("Please do not use semicolon in a string.");
        }
        // Check title length range
        if (p_title.length() == 0) {
            throw new BlankInputException("The title should not be blank.");
        } else if (p_title.length() > 25) {
            throw new TextLengthException("The title text length is out of range.");
        } else {
            return true;
        }
    }

    private boolean isDescValid(String desc) throws TextLengthException, BlankInputException, SemicolonException {
        // Check semicolon
        if (desc.contains(";")) {
            throw new SemicolonException("Please do not use semicolon in a string.");
        }
        // Check title length range
        if (desc.length() == 0) {
            throw new BlankInputException("The description should not be blank.");
        } else if (desc.length() > 70) {
            throw new TextLengthException("The description text length is out of range.");
        } else {
            return true;
        }
    }

    protected boolean renameImage() {
        File file1 = new File("./images/temp.jpg");
        File file2 = new File("./images/" + ID + ".jpg");
        return file1.renameTo(file2);
    }


}
