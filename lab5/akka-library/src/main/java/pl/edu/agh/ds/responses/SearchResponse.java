package pl.edu.agh.ds.responses;

import akka.actor.ActorRef;

import java.io.Serializable;

public class SearchResponse extends Response implements Serializable {

    double price;
    boolean found;
    ActorRef client;

    public SearchResponse(String title, double price, ActorRef client) {
        super(title);
        this.price = price;
        this.found = true;
        this.client = client;
    }

    public SearchResponse(String title, ActorRef client) {
        super(title);
        this.found = false;
        this.client = client;
    }

    public double getPrice() {
        return price;
    }

    public boolean isFound() {
        return found;
    }

    public ActorRef getClient() {
        return client;
    }
}
