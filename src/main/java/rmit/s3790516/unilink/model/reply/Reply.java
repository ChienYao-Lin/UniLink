package rmit.s3790516.unilink.model.reply;

import rmit.s3790516.unilink.model.exception.ReplyFailException;
import rmit.s3790516.unilink.model.exception.StudentNumberException;

public class Reply implements Comparable<Reply> {

    private String post_ID;
    private String responder_ID;
    private String updateTime;
    private float value;

    // create a reply in system
    public Reply(String p_ID, float val, String r_ID) throws ReplyFailException {
        this.post_ID = p_ID;
        if (isValueValid(val)){
            this.value = val;
        }
        this.responder_ID = r_ID;
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.updateTime = sdf.format(dt);
    }

    // create a reply from database
    public Reply(String p_ID, float val, String r_ID, String time) {
        this.post_ID = p_ID;
        this.value = val;
        this.responder_ID = r_ID;
        this.updateTime = time;
    }

    // create a reply from database
    public Reply(String p_ID, String[] data) throws ArrayIndexOutOfBoundsException, NumberFormatException, StudentNumberException, ReplyFailException {
        this.post_ID = p_ID;
        float val = Float.parseFloat(data[1]);
        if (isValueValid(val)){
            this.value = val;
        }
        if (isIDValid(data[2])) {
            this.responder_ID = data[2];
        }
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.updateTime = sdf.format(dt);
    }

    public String getDBDetails() {
        return "'" + post_ID + "', " + value + ", '" + responder_ID + "', '" + updateTime + "'";
    }

    public String getExportDetails() {
        return "Re;" + value + ";" + responder_ID + ";" + updateTime;
    }

    public float getValue() {
        return this.value;
    }

    public String getResponder_ID() {
        return this.responder_ID;
    }

    private boolean isIDValid(String stuID) throws StudentNumberException {
        // check if student ID is valid
        if (stuID.matches("s\\d+")) {
            return true;
        } else {
            throw new StudentNumberException("Student id should begin with the character 's' followed by a number.");
        }
    }

    private boolean isValueValid(float val) throws ReplyFailException {
        // check if Value is valid
        if (val > 0) {
            return true;
        } else {
            throw new ReplyFailException("Reply value should be greater than 0.");
        }
    }

    @Override
    public int compareTo(Reply r) {
        float diff = this.value - r.getValue();
        if (diff > 0) {
            return 1;
        } else if (diff == 0) {
            return 0;
        } else {
            return -1;
        }
    }
}
