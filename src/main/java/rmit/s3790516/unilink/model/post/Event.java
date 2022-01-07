package rmit.s3790516.unilink.model.post;

import rmit.s3790516.unilink.model.exception.*;
import rmit.s3790516.unilink.model.reply.Reply;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Event extends Post {

    private final ArrayList<String> attendee_list;
    private String venue;
    private String date;
    private int capacity;

    // create a new event
    public Event(String p_ID, String p_title, String desc, String c_ID, int cap, String dateString, String venueString
    ) throws TextLengthException, BlankInputException, ParseException, EventDateSetException, NumberNonPositiveException, SemicolonException {
        super(p_ID, p_title, desc, c_ID);
        if (isVenueValid(venueString)) {
            this.venue = venueString;
        }
        if (isCapValid(cap)) {
            this.capacity = cap;
        }
        if (isDateFormat(dateString)) {
            this.date = dateString;
        }
        attendee_list = new ArrayList<String>();
    }

    // create an event from database
    public Event(ResultSet resultSet, ArrayList<Reply> repliesArraylist) throws SQLException {
        super(resultSet, repliesArraylist);
        this.venue = resultSet.getString("VENUE");
        this.date = resultSet.getString("DATE");
        this.capacity = resultSet.getInt("CAPACITY");

        attendee_list = new ArrayList<String>();
        for (Reply reply : replies) {
            attendee_list.add(reply.getResponder_ID());
        }
    }

    // create an event from text file
    public Event(String p_ID, String[] data, ArrayList<Reply> repliesArraylist) throws TextLengthException, StatusTypeException, BlankInputException, ArrayIndexOutOfBoundsException, ParseException, EventDateSetException, NumberNonPositiveException, SemicolonException, StudentNumberException, NumberFormatException {
        super(p_ID, data, repliesArraylist);
        String venueString = data[9];
        String dateString = data[10];
        int cap = Integer.parseInt(data[11]);

        if (isVenueValid(venueString)) {
            this.venue = venueString;
        }
        if (isCapValid(cap)) {
            this.capacity = cap;
        }
        if (isDateFormat(dateString)) {
            this.date = dateString;
        }

        attendee_list = new ArrayList<String>();
        for (Reply reply : replies) {
            attendee_list.add(reply.getResponder_ID());
        }
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venueString) throws BlankInputException, TextLengthException, SemicolonException {
        if (isVenueValid(venueString)) {
            this.venue = venueString;
        }
        update();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String dateString) throws EventDateSetException, ParseException, BlankInputException {
        if (isDateFormat(dateString)) {
            this.date = dateString;
        }
        update();
    }

    public String getCap() {
        return Integer.toString(capacity);
    }

    public void setCapacity(int cap) throws NumberNonPositiveException {
        if (isCapValid(cap)) {
            this.capacity = cap;
        }
        update();
    }

    public String getAttCount() {
        return Integer.toString(attendee_list.size());
    }

    @Override
    protected String getPostDBDetails() {
        return super.getPostDBDetails() + ", '" + venue + "', '" + date + "', " + capacity;
    }

    @Override
    public String getPostUpdateDBDetails() {
        return super.getPostUpdateDBDetails() + ", Venue = '" + venue + "', Date = '" + date + "', Capacity = " + capacity;
    }

    @Override
    public String getPostExportDetails() {
        return super.getPostExportDetails() + ";" + venue + ";" + date + ";" + capacity;
    }

    @Override
    public String handleReply(Reply reply) throws ReplyFailException {
        // If the student have been recorded
        if (attendee_list.indexOf(reply.getResponder_ID()) != -1) {
            throw new ReplyFailException("Registration failed! You have been in the attendence list");
        } else {
            replies.add(reply);
            attendee_list.add(reply.getResponder_ID());
            if (attendee_list.size() == capacity) {
                closePost();
            }
            update();
            return "Event registration accepted!";
        }
    }

    @Override
    public String getReplyInfo() {

        String infoString = "Name: " + getTitle() +
                "\nVenue: " + venue +
                "\nStatus: " + getStatus() + "\n";

        return infoString;
    }

    @Override
    //save this event to database
    public void save(Statement stmt) throws SQLException {
        if (isLoad()) {
            if (isUpdate()) {
                String query = "UPDATE event" +
                        " SET " + getPostUpdateDBDetails() +
                        "WHERE ID LIKE '" + getID() + "'";
                stmt.executeUpdate(query);
            }
        } else {
            String query = "INSERT INTO event" +
                    " VALUES (" + getPostDBDetails() + ")";
            stmt.executeUpdate(query);
        }
    }

    public String getReplyDetails() {
        // initialisation
        String listString = "";

        // If an event has no attendee
        if (attendee_list.size() == 0) {
            listString = "Empty" + "\n";
            // otherwise
        } else {
            for (String atd : attendee_list) {
                listString += atd + ",";
            }
            listString = listString.substring(0, listString.length() - 1);
        }

        return String.format("%-15s %s\n", "Attendee list:", listString);
    }

    private boolean isDateFormat(String date) throws EventDateSetException, ParseException, BlankInputException {
        // Check if date is null
        if (date.trim().equals("")) {
            throw new BlankInputException("The Date should not be blank.");
            // if date is not null
        } else {
            // Set format
            SimpleDateFormat ausFormat = new SimpleDateFormat("dd/MM/yyyy");
            ausFormat.setLenient(false);

            // check the input format
            Date inputDate = ausFormat.parse(date);
            Date today = new Date();
            if (inputDate.after(today)) {
                return true;
            } else {
                throw new EventDateSetException("Event date should be in the future");
            }
        }
    }

    private boolean isCapValid(int cap) throws NumberNonPositiveException {
        // Check cap value
        if (cap <= 0) {
            throw new NumberNonPositiveException("Creation failed: capacity should be greater than 0.");
        } else {
            return true;
        }
    }

    private boolean isVenueValid(String venueString) throws SemicolonException, BlankInputException, TextLengthException {
        // Check semicolon
        if (venueString.contains(";")) {
            throw new SemicolonException("Please do not use semicolon.");
        }
        // Check venue length range
        if (venueString.length() == 0) {
            throw new BlankInputException("Creation failed: The venue should not be blank.");
        } else if (venueString.length() > 40) {
            throw new TextLengthException("Creation failed: The venue text length is out of range.");
        } else {
            return true;
        }
    }
}
