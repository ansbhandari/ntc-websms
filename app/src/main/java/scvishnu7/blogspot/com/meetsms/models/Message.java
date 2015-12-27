package scvishnu7.blogspot.com.meetsms.models;

import java.util.Calendar;

/**
 * Created by vishnu on 12/24/15.
 */
public class Message {
    public int id;
    public String to;
    public String msg;
    public String date;
    public boolean status;

    public Message(){}

    public Message(int id, String msg, String to, boolean status) {
        this.id=id;
        this.to=to;
        this.msg=msg;
        this.status=status;
        this.date= Calendar.getInstance().getTimeInMillis()+"";
    }
}
