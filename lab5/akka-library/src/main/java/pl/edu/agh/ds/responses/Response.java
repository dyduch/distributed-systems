package pl.edu.agh.ds.responses;

import java.io.Serializable;

public abstract class Response implements Serializable {

    public Response(String title) {
        this.title = title;
    }

    String title;

    public String getTitle() {
        return title;
    }
}
