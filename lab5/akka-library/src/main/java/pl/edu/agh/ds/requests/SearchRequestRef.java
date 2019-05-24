package pl.edu.agh.ds.requests;

import akka.actor.ActorRef;

public class SearchRequestRef extends SearchRequest {

    ActorRef client;
    ActorRef from;

    public SearchRequestRef(String title, ActorRef from, ActorRef client) {
        super(title);
        this.client = client;
        this.from = from;
    }

    public ActorRef getClient() {
        return client;
    }

    public ActorRef getFrom() {
        return from;
    }

    public void setFrom(ActorRef from) {
        this.from = from;
    }
}
