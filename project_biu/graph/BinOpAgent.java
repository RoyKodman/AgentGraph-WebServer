package graph;

import graph.TopicManagerSingleton.TopicManager;

import java.util.Objects;
import java.util.function.BinaryOperator;

public class BinOpAgent implements Agent{
    String name;
    Double input1;
    Double input2;
    Topic t1In;
    Topic t2In;
    Topic tOut;
    BinaryOperator<Double> op;

    public BinOpAgent(String nameAgent, String topicIn1Name, String topicIn2Name, String topicOutName, BinaryOperator<Double> operator){
        this.name = nameAgent;
        this.input1 = 0.0;
        this.input2 = 0.0;

        TopicManager tm = TopicManagerSingleton.get();
        this.t1In = tm.getTopic(topicIn1Name);
        this.t2In = tm.getTopic(topicIn2Name);
        this.tOut = tm.getTopic(topicOutName);

        this.op = operator;

        // The agent must subscribe to the input Topics:
        this.t1In.subscribe(this);
        this.t2In.subscribe(this);

        // The agent will publish the result to the output Topic:
        this.tOut.addPublisher(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.input1 = 0.0;
        this.input2 = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        // Understand which input to update:
        if (Objects.equals(topic, t1In.name)){
            input1 = msg.asDouble;
        }
        else if (Objects.equals(topic, t2In.name)){
            input2 = msg.asDouble;
        }
        // Calc the result:
        Double result = op.apply(input1, input2);
        tOut.publish(new Message(result));
    }

    @Override
    public void close() {}
}
