package com.brentondurkee.ccm.events;

/**
 * Created by brenton on 6/3/15.
 */
public class Event {
    String title;
    String location;
    String date;

    public Event(String title, String location, String date){
        this.title = title;
        this.location = location;
        this.date = date;
    }

    public static Event[] createList (int num){
        Event[] output = new Event[num];

        for(int i = 0; i < output.length; i++){
            output[i] = new Event("Message " + i, "Location " + i, "Date " + i);
        }

        return output;
    }
}
