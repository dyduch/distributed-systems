package pl.edu.agh.ds.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import pl.edu.agh.ds.requests.OrderRequest;
import pl.edu.agh.ds.requests.OrderRequestRef;
import pl.edu.agh.ds.requests.SearchRequest;
import pl.edu.agh.ds.requests.SearchRequestRef;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;

public class ServerActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequest.class, rq -> {
                    log.info("Received from client: " + rq);
                    SearchRequestRef rqRef = new SearchRequestRef(rq.getTitle(), getSender(), getSender());
                    log.info("Creted rq " + rqRef.getClient() + " " + rqRef.getTitle());
                    context().child("search_actor").get().tell(rqRef, getSelf());
                })
                .match(OrderRequest.class, rq -> {
                    log.info("Received from client: " + rq);
                    ActorRef searchActorRef = context().child("search_actor").get();
                    OrderRequestRef rqRef = new OrderRequestRef(rq.getTitle(), getSender(), searchActorRef);
                    log.info("Creted rq " + rqRef.getClient() + " " + rqRef.getTitle() + " " + rqRef.getSearchActor());
                    context().child("order_actor").get().tell(rqRef, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchActor.class), "search_actor");
        context().actorOf(Props.create(OrderActor.class), "order_actor");
    }

    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"), DeciderBuilder.
            match(FileNotFoundException.class, o -> resume()).
            match(IOException.class, o -> resume()).
            matchAny(o -> restart()).
            build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

}
