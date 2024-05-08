package com.example.cs4084mobileappdevelopment;

public class Comment {

    private String comment;
    private String author; //might add random username feature
    private int timestamp;

    public Comment() {

    }

    public Comment(String text, String author, int timestamp) {
        this.comment = text;
        this.author = author;
        this.timestamp = timestamp;
    }

    public String getComment() {

        return comment;
    }

    public void setText(String text) {

        this.comment = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getTime() {
        return timestamp;
    }

    public void setTime(int timestamp) {
        this.timestamp = timestamp;
    }
}