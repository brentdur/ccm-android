package com.brentondurkee.ccm.inbox;

/**
 * Created by brenton on 6/3/15.
 */
public class Message {
    String subject;
    String from;
    String time;

    public Message(String subject, String from, String time){
        this.subject = subject;
        this.from = from;
        this.time = time;
    }

    public static Message[] createList (int num){
        Message[] output = new Message[num];

        for(int i = 0; i < output.length; i++){
            output[i] = new Message("Subject " + i, "From " + i, "Time " + i);
        }

        return output;
    }
}
