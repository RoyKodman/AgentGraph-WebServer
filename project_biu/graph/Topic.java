package graph;
import java.util.ArrayList;
import java.util.List;

public class Topic {
    public final String name; // The name of the topic
    public List<Agent> subs; // A list of agents who subscribed to the topic
    public List<Agent> pubs; // A list of agents who will publish to the topic

    Topic(String name) { // The permission of the constructor is package.
        this.name = name;
        this.subs = new ArrayList<Agent>();
        this.pubs = new ArrayList<Agent>();
    }
    // I don't know if the methods are need to be public!
    public void subscribe(Agent agent) {
        this.subs.add(agent);
    }

    public void unsubscribe(Agent agent) {
        this.subs.remove(agent);
    }

    public void publish(Message msg) {
        for (Agent agent : this.subs) {
            agent.callback(this.name, msg);
        }
    }

    public void addPublisher(Agent agent) {
        this.pubs.add(agent);
    }

    public void removePublisher(Agent agent) {
        this.pubs.remove(agent);
    }

}
