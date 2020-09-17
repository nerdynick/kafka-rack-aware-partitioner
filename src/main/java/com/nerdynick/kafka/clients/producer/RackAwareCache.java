package com.nerdynick.kafka.clients.producer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
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

    public List<PartitionInfo> getAvailablePartitions(final String topic, final Cluster cluster, final boolean preferedLeaderOnly){
        final List<PartitionInfo> parts = availablePartitionCache.computeIfAbsent(topic, t->{
            return cluster.availablePartitionsForTopic(t);
        });

        if(preferedLeaderOnly){
            return parts.stream().filter(this::filterPreferedLeader).collect(Collectors.toList());
        } else {
            return parts.stream().filter(this::filter).collect(Collectors.toList());
        }
    }

    private boolean filter(PartitionInfo p){
        return p.leader().hasRack() && p.leader().rack().equals(rack);
    }
    private boolean filterPreferedLeader(PartitionInfo p){
        final Node preferedLeader = p.replicas()[0];
        return p.leader().hasRack() && preferedLeader.rack().equals(rack);
    }

    public void update(final String topic, final Cluster cluster){
        availablePartitionCache.replace(topic, cluster.availablePartitionsForTopic(topic));
        allPartitionCache.replace(topic, cluster.partitionsForTopic(topic));
    }

    public List<PartitionInfo> getPartitions(final String topic, final Cluster cluster, final boolean preferedLeaderOnly){
        final List<PartitionInfo> parts = allPartitionCache.computeIfAbsent(topic, t->{
            return cluster.partitionsForTopic(topic);
        });
        if(preferedLeaderOnly){
            return parts.stream().filter(this::filterPreferedLeader).collect(Collectors.toList());
        } else {
            return parts.stream().filter(this::filter).collect(Collectors.toList());
        }
    }
}
