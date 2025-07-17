package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent{
    protected Agent agent;
    protected BlockingQueue<Message> messagesQueue;
    protected Thread t;
    protected boolean run;

    public ParallelAgent(Agent a, int capacity) {
        this.agent = a;
        this.messagesQueue = new ArrayBlockingQueue<>(capacity);
        this.run = true;

        // Thread which pulls messages one by one from the queue:
        t = new Thread(() -> {
            while (this.run){
                try {
                    Message currentElement = this.messagesQueue.take();
                    // Extracts the topic + message:
                    String[] parts = currentElement.asText.split(" ");
                    String topicName = parts[0];
                    String stringMsg = parts[1];
                    Message msg = new Message(stringMsg);
                    this.agent.callback(topicName, msg);
                }catch (InterruptedException e) {
                    break;
                }
            }
        });
        t.start();
    }

    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        // We want to insert to the queue the topic and the message.
        // So we warped them together to topic + the msg as text.
        try {
            this.messagesQueue.put(new Message(topic+" "+msg.asText));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.run = false;
        this.t.interrupt();
        agent.close();
    }
}
