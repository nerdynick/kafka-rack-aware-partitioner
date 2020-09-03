package com.nerdynick.kafka.clients.producer;

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

public class DefaultRackAwarePartitioner extends AbstractRackAwarePartitioner {
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
        if (keyBytes == null) {
            return stickyPartitionCache.partition(topic, cluster);
        }
        final List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
        // hash the keyBytes to choose a partition
        int partition = Utils.toPositive(Utils.murmur2(keyBytes)) % availablePartitions.size();
        return availablePartitions.get(partition).partition();
    }
    
}
