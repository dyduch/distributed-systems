package pl.edu.agh.ds.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import pl.edu.agh.ds.requests.OrderRequest;
import pl.edu.agh.ds.requests.Request;
import pl.edu.agh.ds.requests.SearchRequest;

public class Client {
    public static void main(String[] args) throws IOException {

        File configFile = new File("conf/client.conf");
        Config config = ConfigFactory.parseFile(configFile);

        final ActorSystem system = ActorSystem.create("client_system", config);
        final ActorRef clientActor = system
                .actorOf(Props.create(ClientActor.class), "client_actor");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            Request request;
            if (line.equals("q")) {
                break;
            }
            if(line.startsWith("search")){
                String title = br.readLine();
                request = new SearchRequest(title);
                clientActor.tell(request, null);
            }

            if(line.startsWith("order")){
                String title = br.readLine();
                request = new OrderRequest(title);
                clientActor.tell(request, null);
            }
            //clientActor.tell(line, null);
        }

        system.terminate();
    }
}
