package com.example.blackops.imageuploader;

/**
 * Created by aniru on 13-10-2016.
 */

public class PendingNote {
    String courseId;
    String uriList;
    int noOfPagesUploaded;
    public PendingNote(String cId, String uList, int pages) {
        this.courseId = cId;
        this.uriList = uList;
        this.noOfPagesUploaded = pages;
    }

    public String getUriList() {
        return this.uriList;
    }

    public void setUriList(String uList) {
        this.uriList = uList;
    }

    public String getCourseId() {
        return this.courseId;
    }

    public void setCourseId(String cId) {
        this.courseId = cId;
    }

    public int getNoOfPagesUploaded() {
        return this.noOfPagesUploaded;
    }

    public void setNoOfPagesUploaded(int pages) {
        this.noOfPagesUploaded = pages;
    }
}
