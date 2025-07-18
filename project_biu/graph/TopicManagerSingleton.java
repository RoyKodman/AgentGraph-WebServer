package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    public static class TopicManager{
        // Thread-safe map of topic names to Topic objects
        private ConcurrentHashMap<String, Topic> topicsMap;
        // Map of topic names to their latest Message (thread-safe)
        private ConcurrentHashMap<String, Message> leastValuesMap;

        private TopicManager(){
            // Initialize the maps
            this.topicsMap = new ConcurrentHashMap<>();
            this.leastValuesMap = new ConcurrentHashMap<>();
        }

        // Get or create a Topic by name
        public boolean hasTopic(String topicName){
            return this.topicsMap.containsKey(topicName);
        }

        public Topic getTopic(String topicName){
            return this.topicsMap.computeIfAbsent(topicName, Topic::new);
        }

        // Get all topics as a collection
        public Collection<Topic> getTopics(){
            return new ArrayList<>(topicsMap.values());
        }

        // Get the map of latest messages for each topic
        public ConcurrentHashMap<String, Message> getLeastValuesMap(){
            return this.leastValuesMap;
        }

        // Update the latest message for a topic
        public void addLeastValue(String topicName, Message message){
            this.leastValuesMap.put(topicName, message);
        }

        // Remove all topics
        public void clear(){
            this.topicsMap.clear();
        }
    }

    // Singleton instance
    private static final TopicManager instance = new TopicManager();

    // Accessor for the singleton instance
    public static TopicManager get(){
        return instance;
    }

}
