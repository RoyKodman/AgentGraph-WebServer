package graph;

import java.util.ArrayList;

import graph.TopicManagerSingleton.TopicManager;

public class Graph extends ArrayList<Node>{

    // Function who get graph and agent and return the node who represent the agent if exist,
    // and creat it otherwise.
    private Node getNodeAgent(Agent a) {
        for (Node n : this) {
            if (n.getAgent() == a) return n;
        }
        // Unique id based on the Java object identity
        String id = "A" + a.getName() + "_" + System.identityHashCode(a);
        Node newAgentNode = new Node(id);
        newAgentNode.setAgent(a);
        this.add(newAgentNode);
        return newAgentNode;
    }

    public boolean hasCycles() {
        for (Node n : this) {
            if (n.hasCycles()){
                return true;
            }
        }
        return false;
    }
    public void createFromTopics(){
        // Loop over all the topics:
        TopicManager tm = TopicManagerSingleton.get();
        for (Topic t : tm.getTopics()){
            // For each topic, add it as a node:
            Node topicNode = new Node("T" + t.name);

            for (Agent a : t.subs){
                // If the agent not at the graph we need to add him:
                Node subNode = this.getNodeAgent(a);
                topicNode.addEdge(subNode);
            }
            this.add(topicNode);

            for (Agent a : t.pubs){
                // If the agent not at the graph we need to add him:
                Node pubNode = this.getNodeAgent(a);
                pubNode.addEdge(topicNode);
            }

        }
    }
}
