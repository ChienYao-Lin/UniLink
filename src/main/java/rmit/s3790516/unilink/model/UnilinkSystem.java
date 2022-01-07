package rmit.s3790516.unilink.model;

import rmit.s3790516.unilink.model.exception.*;
import rmit.s3790516.unilink.model.post.Event;
import rmit.s3790516.unilink.model.post.Job;
import rmit.s3790516.unilink.model.post.Post;
import rmit.s3790516.unilink.model.post.Sale;
import rmit.s3790516.unilink.model.reply.Reply;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UnilinkSystem {
    private final Map<String, Post> posts;
    private final String DB_NAME;
    private final int[] postNum;

    public UnilinkSystem(String DBString) throws ClassNotFoundException, SQLException {
        posts = new HashMap<String, Post>();
        DB_NAME = DBString;
        postNum = new int[3];
    }

    public Collection<Post> getPosts() {
        return posts.values();
    }

    public String getNewPostID(String type) throws PostTypeException {
        switch (type) {
            case "EVE":
                return "EVE" + String.format("%03d", postNum[0] + 1);
            case "SAL":
                return "SAL" + String.format("%03d", postNum[1] + 1);
            case "JOB":
                return "JOB" + String.format("%03d", postNum[2] + 1);
            default:
                throw new PostTypeException("No post with type " + type);
        }
    }

    public void addPost(Post post) {
        if (posts.containsKey(post.getID())) {
            System.out.println("already exist");
        } else {
            posts.put(post.getID(), post);
            if (post instanceof Event) {
                postNum[0]++;
            } else if (post instanceof Sale) {
                postNum[1]++;
            } else {
                postNum[2]++;
            }
        }
    }

    // import one post from text file with its replies
    public void importPost(String PID, String[] data, int repliesCount, String replyRows) throws TextLengthException, StatusTypeException,
            BlankInputException, ParseException, EventDateSetException, NumberNonPositiveException, SemicolonException, StudentNumberException,
            NumberFormatException, ReplyFailException {

        if (PID.contains("EVE")) {
            posts.put(PID, new Event(PID, data, loadReplies(PID, repliesCount, replyRows)));
            postNum[0]++;
        } else if (PID.contains("SAL")) {
            posts.put(PID, new Sale(PID, data, loadReplies(PID, repliesCount, replyRows)));
            postNum[1]++;
        } else if (PID.contains("JOB")) {
            posts.put(PID, new Job(PID, data, loadReplies(PID, repliesCount, replyRows)));
            postNum[2]++;
        }
    }

    // export all posts and replies to text file
    public void exportData(File file) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);
        for (Post post : posts.values()) {
            writer.println(post.getPostExportDetails());
            for (int i = 0; i < post.getReplyCount(); i++) {
                writer.println(post.getReplyExportDetails(i));
            }
        }
        writer.close();
    }

    // load posts from database
    public void loadData() throws SQLException, ClassNotFoundException {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection con = DriverManager.getConnection
                ("jdbc:hsqldb:file:database/" + DB_NAME, "SA", "");

        loadEvents(con);
        loadSales(con);
        loadJobs(con);
        con.close();
    }

    //save posts to database
    public void saveData() throws SQLException, ClassNotFoundException {

        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        Connection con = DriverManager.getConnection
                ("jdbc:hsqldb:file:database/" + DB_NAME, "SA", "");

        Statement stmt = con.createStatement();

        for (Post post : posts.values()) {
            post.save(stmt);
            post.saveReplies(stmt);
        }

        con.close();
    }

    // load events from database
    private void loadEvents(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String lastID = "EVE000";
        String query;
        query = "SELECT * FROM event ";

        try (ResultSet resultSet = stmt.executeQuery(query)) {
            while (resultSet.next()) {
                String PID = resultSet.getString("ID");
                posts.put(PID, new Event(resultSet, loadReplies(PID, con)));
                if (lastID.compareTo(PID) < 0) {
                    lastID = PID;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        postNum[0] = Integer.parseInt(lastID.substring(3));
    }

    // load sales from database
    private void loadSales(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String lastID = "SAL000";
        String query;

        query = "SELECT * FROM sale ";

        try (ResultSet resultSet = stmt.executeQuery(query)) {
            while (resultSet.next()) {
                String PID = resultSet.getString("ID");
                posts.put(PID, new Sale(resultSet, loadReplies(PID, con)));
                if (lastID.compareTo(PID) < 0) {
                    lastID = PID;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        postNum[1] = Integer.parseInt(lastID.substring(3));
    }

    // load jobs from database
    private void loadJobs(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String lastID = "JOB000";
        String query;

        query = "SELECT * FROM job ";

        try (ResultSet resultSet = stmt.executeQuery(query)) {
            while (resultSet.next()) {
                String PID = resultSet.getString("ID");
                String time = resultSet.getString("TIME");
                posts.put(PID, new Job(resultSet, loadReplies(PID, con)));
                if (lastID.compareTo(PID) < 0) {
                    lastID = PID;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        postNum[2] = Integer.parseInt(lastID.substring(3));
    }

    // load replies from database
    public ArrayList<Reply> loadReplies(String PID, Connection con) throws SQLException {
        ArrayList<Reply> replies = new ArrayList<Reply>();
        Statement stmt = con.createStatement();
        String query;
        query = "SELECT * FROM reply " +
                "WHERE POST_ID = '" + PID + "'" +
                "ORDER BY UPDATETIME";

        try (ResultSet resultSet = stmt.executeQuery(query)) {
            while (resultSet.next()) {
                float value = resultSet.getFloat("VALUE");
                String R_ID = resultSet.getString("RESPONDER_ID");
                String time = resultSet.getString("UPDATETIME");
                replies.add(new Reply(PID, value, R_ID, time));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return replies;
    }

    //load replies from text file
    private ArrayList<Reply> loadReplies(String PID, int repliesCount, String replyRows) throws ReplyFailException, StudentNumberException {
        ArrayList<Reply> replies = new ArrayList<Reply>();
        if (repliesCount > 0){
            String[] rows = replyRows.split("\n");
            if (repliesCount != rows.length) {
                throw new ReplyFailException("Data of the post does not match the the number of given replies");
            }
            for (String row : rows) {
                String[] data = row.split(";");
                if (data[0].equals("Re")){
                    replies.add(new Reply(PID, data));
                } else {
                    throw new ReplyFailException("The Type of Replies is not valid.");
                }
            }
        } else if (repliesCount < 0) {
            throw new ReplyFailException("The count number of replies should be 0 or positive integer.");
        }
        return replies;
    }
}
