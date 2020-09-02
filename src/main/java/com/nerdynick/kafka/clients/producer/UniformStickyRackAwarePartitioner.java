package com.nerdynick.kafka.clients.producer;

import java.util.Map;

import org.apache.kafka.common.Cluster;

public class UniformStickyRackAwarePartitioner extends AbstractRackAwarePartitioner {
    private RackAwareStickyPartitionCache stickyPartitionCache;

    @Override
    public void configure(Map<String, ?> configs) {
        super.configure(configs);
        stickyPartitionCache = new RackAwareStickyPartitionCache(partitionCache);
    }
    
    @Override
    public void close() {
    }

    @Override
    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        super.onNewBatch(topic, cluster, prevPartition);
        stickyPartitionCache.nextPartition(topic, cluster, prevPartition);
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        return stickyPartitionCache.partition(topic, cluster);
    }
}
