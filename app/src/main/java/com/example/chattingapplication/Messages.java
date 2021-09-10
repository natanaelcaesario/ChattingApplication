package com.example.chattingapplication;

public class Messages {

    private String message;
    private String type;
    private String from;
    private boolean seen;
    private long time;
    public Messages(String from) {
        this.from = from;
    }
    public Messages(String message, boolean seen, long time, String type){
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
    public long getTime() { return time; }
    public void setTime(Long time) { this.time = time; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Messages() {}

}
