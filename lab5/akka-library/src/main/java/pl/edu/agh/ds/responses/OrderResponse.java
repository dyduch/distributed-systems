package pl.edu.agh.ds.responses;

import java.io.Serializable;

public class OrderResponse extends Response implements Serializable {

    boolean ordered;

    public OrderResponse(String title, boolean ordered) {
        super(title);
        this.ordered = ordered;
    }

    public boolean isOrdered() {
        return ordered;
    }
}
