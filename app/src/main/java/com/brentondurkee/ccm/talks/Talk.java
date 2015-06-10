package com.brentondurkee.ccm.talks;

/**
 * Created by brenton on 6/4/15.
 */
public class Talk {
    String topic;
    String author;
    String time;
    String verse;

    public Talk(String topic, String author, String time, String verse){
        this.topic = topic;
        this.author = author;
        this.time = time;
        this.verse = verse;
    }

    public static Talk[] createList (int num){
        Talk[] output = new Talk[num];

        for(int i = 0; i < output.length; i++){
            output[i] = new Talk("topic " + i, "author " + i, "Time " + i, "verse " + i);
        }

        return output;
    }
}
