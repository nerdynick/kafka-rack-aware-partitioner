package com.nerdynick.kafka.clients.producer;

import java.util.Map;

import org.apache.kafka.clients.producer.UniformStickyPartitioner;
import org.apache.kafka.common.Cluster;

/**
 * A round robin partitioner that limits publication to only those partitions who's leader broker exists in a given rack.
 * Record distribution is uniform.
 * 
 * @see UniformStickyPartitioner for further details.
 * 
 * NOTE: Keys are ignored, resulting in like keys not being guranteed to be placed on the same partition.
 */
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
