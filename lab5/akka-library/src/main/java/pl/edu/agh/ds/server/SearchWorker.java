package pl.edu.agh.ds.server;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.ds.requests.SearchRequest;
import pl.edu.agh.ds.requests.SearchRequestRef;
import pl.edu.agh.ds.responses.SearchResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.LogManager;


public class SearchWorker extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    int dbNumber;
    String dbPath;

    public SearchWorker(Integer dbNumber) {
        this.dbNumber = dbNumber;
        this.dbPath = "src/main/resources/databases/db" + dbNumber + ".txt";
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchRequestRef.class, srq -> {
                    SearchResponse response;
                    String title = srq.getTitle();
                    double price = findPrice(title);
                    if(price == -1){
                        response = new SearchResponse(title, srq.getClient());
                    } else {
                        response = new SearchResponse(title, price, srq.getClient());
                    }
                    srq.getFrom().tell(response, getSelf());
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }

    private double findPrice(String title) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(dbPath));
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.startsWith(title)){
                return Double.parseDouble(line.split(" ")[1]);
            }
        }
        return -1;
    }
}
