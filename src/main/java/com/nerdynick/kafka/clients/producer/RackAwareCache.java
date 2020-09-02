package com.nerdynick.kafka.clients.producer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

public class RackAwareCache {
    private final ConcurrentMap<String, List<PartitionInfo>> availablePartitionCache;
    private final ConcurrentMap<String, List<PartitionInfo>> allPartitionCache;
    private final String rack;

    public RackAwareCache(final String rack){
        this.rack = rack;
        availablePartitionCache = new ConcurrentHashMap<>();
        allPartitionCache = new ConcurrentHashMap<>();
    }

    public List<PartitionInfo> getAvailablePartitions(final String topic, final Cluster cluster){
        return availablePartitionCache.computeIfAbsent(topic, t->{
            return this.filter(cluster.availablePartitionsForTopic(topic));
        });
    }

    private List<PartitionInfo> filter(List<PartitionInfo> partitions){
        if(rack != null){
            return partitions.stream().filter(p->{
                return p.leader().hasRack() && p.leader().rack().equals(rack);
            }).collect(Collectors.toList());
        }
        return partitions;
    }

    public void update(final String topic, final Cluster cluster){
        availablePartitionCache.replace(topic, this.filter(cluster.availablePartitionsForTopic(topic)));
        allPartitionCache.replace(topic, this.filter(cluster.partitionsForTopic(topic)));
    }

    public List<PartitionInfo> getPartitions(final String topic, final Cluster cluster){
        return allPartitionCache.computeIfAbsent(topic, t->{
            return this.filter(cluster.partitionsForTopic(topic));
        });
    }
}
