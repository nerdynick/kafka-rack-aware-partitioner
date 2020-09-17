package com.nerdynick.kafka.clients.producer;

import java.util.Map;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.Cluster;

public abstract class AbstractRackAwarePartitioner implements RackAwarePartitioner {
    protected RackAwareCache partitionCache;
    private boolean isRackAware = false;

    /**
     * Lookup the Client Rack value. 
     * If no value is present then Partitioner will default to traditional logic.
     */
    @Override
    public void configure(Map<String, ?> configs) {
        Object r = configs.get(CommonClientConfigs.CLIENT_RACK_CONFIG);
        if (r != null){
            isRackAware = true;
            partitionCache = new RackAwareCache(r.toString());
        }
        partitionCache = new RackAwareCache(null);
    }

    /**
     * Check is Rack Awareness was enabled for this partitioner. 
     * NOTE: configure() must have already been called to actually have a true response.
     * @return
     */
    public boolean isRackAware(){
        return isRackAware;
    }

    public RackAwareCache getPartitionCache(){
        return partitionCache;
    }

    @Override
    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        partitionCache.update(topic, cluster);
    }
}
