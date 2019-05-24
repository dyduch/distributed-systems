package pl.edu.agh.ds.requests;

import java.io.Serializable;

public abstract class Request implements Serializable {

    public Request(String title) {
        this.title = title;
    }

    String title;

    public String getTitle() {
        return title;
    }
}
