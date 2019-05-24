package pl.edu.agh.ds.requests;

import akka.actor.ActorRef;

public class OrderRequestRef extends OrderRequest {

    ActorRef client;
    ActorRef searchActor;

    public OrderRequestRef(String title, ActorRef client, ActorRef searchActor) {
        super(title);
        this.client = client;
        this.searchActor = searchActor;
    }

    public ActorRef getClient() {
        return client;
    }

    public ActorRef getSearchActor() {
        return searchActor;
    }
}
