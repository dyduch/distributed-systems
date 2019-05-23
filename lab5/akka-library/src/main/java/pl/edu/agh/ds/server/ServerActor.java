package pl.edu.agh.ds.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ServerActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    String result = "result " + s.toUpperCase();
                    getSender().tell(result, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
