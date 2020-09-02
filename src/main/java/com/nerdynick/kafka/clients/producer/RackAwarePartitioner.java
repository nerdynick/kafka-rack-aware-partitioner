package com.nerdynick.kafka.clients.producer;

import java.util.Map;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public abstract class RackAwarePartitioner implements Partitioner {
    protected RackAwareCache partitionCache;

    @Override
    public void configure(Map<String, ?> configs) {
        Object r = configs.get(CommonClientConfigs.CLIENT_RACK_CONFIG);
        if (r != null){
            partitionCache = new RackAwareCache(r.toString());
        }
        partitionCache = new RackAwareCache(null);
    }
    
    @Override
    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        partitionCache.update(topic, cluster);
    }
}
