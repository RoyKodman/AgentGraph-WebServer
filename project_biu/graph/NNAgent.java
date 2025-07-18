package graph;
    
import graph.TopicManagerSingleton.TopicManager;

import java.util.Objects;

public class NNAgent implements Agent{
    Double x = 0.0;
    Double y = 0.0;
    Double z = 0.0; // Not used in this example, but can be used for additional inputs
    Double w1 = 1.1;
    Double w2 = 2.4;
    Double w3 = 3.2; // Not used in this example, but can
    Topic t1In;
    Topic t2In;
    Topic t3In; // Not used in this example, but can be used for additional inputs
    Topic tOut;

    public NNAgent(String[] subs, String[] pubs){
        TopicManager tm = TopicManagerSingleton.get();
        this.t1In = tm.getTopic(subs[0]);
        this.t2In = tm.getTopic(subs[1]);
        this.t3In = tm.getTopic(subs[2]);
        this.tOut = tm.getTopic(pubs[0]);

        // The agent must subscribe to the input Topics:
        this.t1In.subscribe(this);
        this.t2In.subscribe(this);
        this.t3In.subscribe(this); // Optional, if you want to use it later
        // The agent will publish the result to the output Topic:
        this.tOut.addPublisher(this);
    }

    @Override
    public String getName() {
        return "NNAgent";
    }

    @Override
    public void reset() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0; // Resetting z, even though it's not used in this example
    }

    @Override
    public void callback(String topic, Message msg) {
        // Understand which input to update:
        if (Objects.equals(topic, t1In.name)){
            x = msg.asDouble;
        }
        else if (Objects.equals(topic, t2In.name)){
            y = msg.asDouble;
        } else if (Objects.equals(topic, t3In.name)){
            z = msg.asDouble; // Not used in this example, but can be used for additional inputs
        } else {
            System.out.println("NNAgent received message on unknown topic: " + topic);
            return; // Ignore messages on unknown topics
        }
        // Calc the result:
        double result = w1*x + w2*y + w3*z;
        tOut.publish(new Message(result));
    }

    @Override
    public void close() {

    }
}
