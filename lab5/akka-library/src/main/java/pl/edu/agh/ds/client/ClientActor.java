package pl.edu.agh.ds.client;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ClientActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(String.class, s -> {
                    if(s.startsWith("result")){
                        System.out.println("Received from remote: " + s);
                    } else {
                        System.out.println("Sending to remote: " + s);
                        getContext().actorSelection("akka.tcp://server_system@127.0.0" +
                                ".1:3552/user/server_actor").tell(s, getSelf());
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
