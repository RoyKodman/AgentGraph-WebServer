package graph;

import java.util.*;


public class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    // Constructor:name
    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
    }

    // Set functions:
    public void setName(String name) {
        this.name = name;
    }

    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }

    public void setMessage(Message msg) {
        this.msg = msg;
    }

    // Get functions:
    public String getName() {
        return this.name;
    }

    public List<Node> getEdges() {
        return this.edges;
    }

    public Message getMessage() {
        return this.msg;
    }

    public void addEdge(Node newEdge) {
        this.edges.add(newEdge);
    }

    // A function to determinate if the graph has cycles:
    public boolean hasCycles() {
        return isPartOfCycle(this, new HashSet<Node>(), new HashSet<Node>());
    }


    // Help function to help us to determinate if is there a cycle:
    private boolean isPartOfCycle(Node node, Set<Node> visited, Set<Node> recursionStack) {
        if (recursionStack.contains(node)) {
            return true;
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        for (Node neighbor : node.edges) {
            if (isPartOfCycle(neighbor, visited, recursionStack)) {
                return true;
            }
        }
        recursionStack.remove(node);
        return false;
    }
}
