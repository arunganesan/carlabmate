package edu.umich.carlab.hal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Very simple class that keeps track of data listeners
 * It doesn't actually know how to start anything. It just returns
 * true or false if something needs to be started.
 */

public class ListenerMap {
    private Map<String, Integer> listenerCounts;

    public ListenerMap () {
        this.listenerCounts = new HashMap<>();
    }

    /**
     * Announces all current listeners
     */
    public Set<String> returnAllListeners () {
        return listenerCounts.keySet();
        //Constants.EmitState(Constants.ALL_LISTENERS, "OK");
    }


    public int howManyListeners(String source) {
        if (!listenerCounts.containsKey(source)) return 0;
        else return listenerCounts.get(source);
    }


    /**
     * Adds the data source to list of listened objects.
     *
     * @param dataSource
     * @return True if this is the first person to listen to it
     */
    public boolean addSubscriber (String dataSource) {
        if (!listenerCounts.containsKey(dataSource)) {
            //Constants.EmitState(Constants.NEW_LISTENER, dataSource);
            listenerCounts.put(dataSource, 0);
        }
        listenerCounts.put(dataSource, listenerCounts.get(dataSource) + 1);
        return (listenerCounts.get(dataSource) == 1);
    }


    /**
     * Removes subscriber from our list  of listeners
     *
     * @param dataSource
     * @return True if this is the last person to listen to it
     */
    public boolean removeSubscriber (String dataSource) {
        // This should never happen -- it means we're double freeing
        // But in case it does, the best response is to return "true"
        if (!listenerCounts.containsKey(dataSource)) return true;


        // Else it contains it
        listenerCounts.put(dataSource, listenerCounts.get(dataSource) - 1);
        if (listenerCounts.get(dataSource) <= 0) {
            listenerCounts.remove(dataSource);
            //Constants.EmitState(Constants.DEL_LISTENER, dataSource);
            return true;
        }

        // Else, we still have listeners.
        return false;
    }
}
