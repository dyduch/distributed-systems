package pl.edu.agh.ds.server;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import pl.edu.agh.ds.requests.SearchRequest;
import pl.edu.agh.ds.requests.SearchRequestRef;
import pl.edu.agh.ds.responses.SearchResponse;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;

public class SearchActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequestRef.class, srq -> {
                    if(srq.getFrom().equals(context().parent())){
                        srq.setFrom(self());
                    }
                    context().child("search_worker1").get().tell(srq, getSelf());
                    context().child("search_worker2").get().tell(srq, getSelf());
                })
                .match(SearchResponse.class, srs -> {
                    srs.getClient().tell(srs, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    @Override
    public void preStart() throws Exception {
        context().actorOf(Props.create(SearchWorker.class, 1), "search_worker1");
        context().actorOf(Props.create(SearchWorker.class, 2), "search_worker2");
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
