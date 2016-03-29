package com.example.Allu.myapplication.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class TaskBean {

    private String myData;
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return myData;
    }

    public void setData(String data) {
        myData = data;
    }
}