package pl.edu.agh.ds.client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.ds.requests.OrderRequest;
import pl.edu.agh.ds.requests.SearchRequest;
import pl.edu.agh.ds.responses.OrderResponse;
import pl.edu.agh.ds.responses.SearchResponse;

public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, rq -> {
                    getContext().actorSelection("akka.tcp://server_system@127.0.0" +
                            ".1:3552/user/server_actor").tell(rq, getSelf());
                })
                .match(SearchResponse.class, rs -> {
                    if (rs.isFound()) {
                        System.out.println("Response from: " + getSender() + ": " + rs.getTitle() + " " + rs.getPrice());
                    } else {
                        System.out.println("Response from: " + getSender() + ": " + rs.getTitle() + " not found");
                    }
                })
                .match(OrderRequest.class, rq -> {
                    getContext().actorSelection("akka.tcp://server_system@127.0.0" +
                            ".1:3552/user/server_actor").tell(rq, getSelf());
                })
                .match(OrderResponse.class, rs -> {
                    System.out.println("Response from: " + getSender() + ": " + rs.getTitle() + " order status: " + rs.isOrdered());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
