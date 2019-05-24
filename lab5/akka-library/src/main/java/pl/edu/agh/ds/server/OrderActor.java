package pl.edu.agh.ds.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import pl.edu.agh.ds.requests.OrderRequestRef;
import pl.edu.agh.ds.requests.SearchRequestRef;
import pl.edu.agh.ds.responses.OrderResponse;
import pl.edu.agh.ds.responses.SearchResponse;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;

public class OrderActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    boolean appended = false;
    String ordersPath = "src/main/resources/orders.txt";

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderRequestRef.class, rq -> {
                    ActorRef searchActor = rq.getSearchActor();
                    searchActor.tell(new SearchRequestRef(rq.getTitle(), getSelf(), rq.getClient()), getSelf());
                })
                .match(SearchResponse.class, rs -> {
                    if(!appended){
                        if(rs.isFound()) {
                            appended = true;
                            appendToOrders(rs.getTitle());
                            rs.getClient().tell(new OrderResponse(rs.getTitle(), true), self());
                        } else {
                            rs.getClient().tell(new OrderResponse(rs.getTitle(), false), self());
                        }
                    } else {
                        appended = false;
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private void appendToOrders(String title) throws IOException {
        Writer fileWriter = new BufferedWriter(new FileWriter(ordersPath, true));
        fileWriter.append(title).append("\n");
        fileWriter.close();
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
