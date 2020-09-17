package com.nerdynick.kafka.clients.producer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

/**
 * A round robin partitioner that limits publication to only those partitions who's leader broker exists in a given rack.
 * Record distribution is not uniform and each record will be placed on different partitions as they come in.
 * 
 * @see RoundRobinPartitioner for more details
 * 
 * NOTE: Keys are ignored, resulting in like keys not being guranteed to be placed on the same partition.
 */
public class RoundRobinRackAwarePartitioner extends AbstractRackAwarePartitioner {
    private final ConcurrentMap<String, AtomicInteger> topicCounterMap = new ConcurrentHashMap<>();


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions =  this.partitionCache.getPartitions(topic, cluster);
        int nextValue = nextValue(topic);
        List<PartitionInfo> availablePartitions = this.partitionCache.getAvailablePartitions(topic, cluster, false);
        if (!availablePartitions.isEmpty()) {
            int part = Utils.toPositive(nextValue) % availablePartitions.size();
            return availablePartitions.get(part).partition();
        } else {
            // no partitions are available, give a non-available partition
            int part = Utils.toPositive(nextValue) % partitions.size();
            return partitions.get(part).partition();
        }
    }

    private int nextValue(String topic) {
        AtomicInteger counter = topicCounterMap.computeIfAbsent(topic, k -> {
            return new AtomicInteger(0);
        });
        return counter.getAndIncrement();
    }

    @Override
    public void close() {}
    
}
