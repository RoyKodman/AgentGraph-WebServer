package graph;

import graph.TopicManagerSingleton.TopicManager;

import java.util.Objects;


public class SigmoidAgent implements Agent{
    Topic tIn;
    Topic tOut;

    public SigmoidAgent(String[] subs, String[] pubs){
        TopicManager tm = TopicManagerSingleton.get();
        this.tIn = tm.getTopic(subs[0]);
        this.tOut = tm.getTopic(pubs[0]);

        // The agent must subscribe to the input Topics:
        this.tIn.subscribe(this);

        // The agent will publish the result to the output Topic:
        this.tOut.addPublisher(this);
    }

    @Override
    public String getName() {
        return "SigmoidAgent";
    }

    @Override
    public void reset() {
    }

    @Override
    public void callback(String topic, Message msg) {
        // Understand which input to update:
        if (Objects.equals(topic, tIn.name)){
            double x = msg.asDouble;
            // Calc the result:
            double s = 1 / (1 + Math.exp(-x)); // Sigmoid function
            tOut.publish(new Message(s));
        }

    }

    @Override
    public void close() {

    }

}
