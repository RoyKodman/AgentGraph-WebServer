package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    public static class TopicManager{
        private ConcurrentHashMap<String, Topic> topicsMap;

        private TopicManager(){
            this.topicsMap = new ConcurrentHashMap<>();
        }

        public Topic getTopic(String topicName){ // Need to be threads safety!
            return this.topicsMap.computeIfAbsent(topicName, Topic::new);
        }

        public Collection<Topic> getTopics(){
            return new ArrayList<>(topicsMap.values());
        }

        public void clear(){
            this.topicsMap.clear();
        }
    }

    private static final TopicManager instance = new TopicManager();

    public static TopicManager get(){
        return instance;
    }

}
