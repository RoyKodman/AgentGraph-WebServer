package configs;


import graph.Agent;
import graph.ParallelAgent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Constructor;


public class GenericConfig implements Config{
    String confFileLocation;
    List<ParallelAgent> pAgentList;
    int agentsCap = 100;
    List<String> lines; //

    public void setConfFile(String confFileLocation){
        this.confFileLocation = confFileLocation;
    }

    @Override
    // Create the configuration.
    public void create() {
        if(confFileLocation!=null){
            try {
                this.pAgentList = new ArrayList<ParallelAgent>();
                // Read all the lines of the file into a list.
                lines = Files.readAllLines(Paths.get(this.confFileLocation));
                // Check that the number of lines div in 3 -> indicate that the file correct.
                if (lines.size() % 3 == 0){
                    for(int i = 0; i < (lines.size() / 3); i++){
                        // Extract the data.
                        String agentLocation = lines.get(i*3);
                        String[] pubsList = lines.get(i*3+1).split(",");
                        String[] subsList = lines.get(i*3+2).split(",");

                        try {
                            // Create agent.
                            Class<?> agentClass = Class.forName(agentLocation);
                            Constructor<?> ctor = agentClass.getConstructor(String[].class, String[].class);
                            Object agent = ctor.newInstance((Object) pubsList, (Object) subsList);
                            ParallelAgent pAgent = new ParallelAgent((Agent) agent, this.agentsCap);
                            this.pAgentList.add(pAgent);

                        } catch (ClassNotFoundException |
                                 NoSuchMethodException   |
                                 InstantiationException  |
                                 IllegalAccessException   |
                                 InvocationTargetException e) {System.out.println(e);}

                    }
                }
            } catch (IOException e){}
        }
    }

    @Override
    public String getName() {
        return this.confFileLocation;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void close() {
        for(ParallelAgent pa : this.pAgentList){
            pa.close();
        }
    }
}
