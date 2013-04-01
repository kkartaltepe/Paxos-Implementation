package edu.utexas.kkartal.chat.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: kurt
 * Date: 3/30/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatMessage implements Serializable {

    private String name;
    private String body;
    private Date time;

    public ChatMessage(String name, String body, Date time){
        this.name = name;
        this.body = body;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public Date getTime() {
        return time;
    }

}
