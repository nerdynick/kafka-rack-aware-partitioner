package com.nerdynick.kafka.clients.producer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

public class RoundRobinRackAwarePartitioner extends AbstractRackAwarePartitioner {
    private final ConcurrentMap<String, AtomicInteger> topicCounterMap = new ConcurrentHashMap<>();


    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions =  this.partitionCache.getPartitions(topic, cluster);
        int nextValue = nextValue(topic);
        List<PartitionInfo> availablePartitions = this.partitionCache.getAvailablePartitions(topic, cluster);
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
