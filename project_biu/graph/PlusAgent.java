package graph;
    
import graph.TopicManagerSingleton.TopicManager;

import java.util.Objects;

public class PlusAgent implements Agent{
    Double x = 0.0;
    Double y = 0.0;
    Topic t1In;
    Topic t2In;
    Topic tOut;

    public PlusAgent(String[] subs, String[] pubs){
        TopicManager tm = TopicManagerSingleton.get();
        this.t1In = tm.getTopic(subs[0]);
        this.t2In = tm.getTopic(subs[1]);
        this.tOut = tm.getTopic(pubs[0]);

        // The agent must subscribe to the input Topics:
        this.t1In.subscribe(this);
        this.t2In.subscribe(this);

        // The agent will publish the result to the output Topic:
        this.tOut.addPublisher(this);
    }

    @Override
    public String getName() {
        return "PlusAgent";
    }

    @Override
    public void reset() {
        this.x = 0.0;
        this.y = 0.0;

    }

    @Override
    public void callback(String topic, Message msg) {
        // Understand which input to update:
        if (Objects.equals(topic, t1In.name)){
            x = msg.asDouble;
        }
        else if (Objects.equals(topic, t2In.name)){
            y = msg.asDouble;
        }
        // Calc the result:
        double result = x + y;
        tOut.publish(new Message(result));
    }

    @Override
    public void close() {

    }
}
