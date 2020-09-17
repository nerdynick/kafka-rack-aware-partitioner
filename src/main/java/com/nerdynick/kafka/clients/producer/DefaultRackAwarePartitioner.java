package com.nerdynick.kafka.clients.producer;

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

/**
 * A version of DefaultPartitioner that provides logic needed to manage Rack ware production. 
 * 
 * @see UniformStickyRackAwarePartitioner for details on Keyless record handling
 * 
 * For keyed records, the prefered leader will be checked for rack presence to be within the configure rack.
 * This to ensure that during a failover that the # of partitions to hash against doesn't change when the leader moves.
 */
public class DefaultRackAwarePartitioner implements RackAwarePartitioner {
    private UniformStickyRackAwarePartitioner nonKeyedPartitioner = new UniformStickyRackAwarePartitioner();

    @Override
    public void configure(Map<String, ?> configs) {
        nonKeyedPartitioner.configure(configs);
    }
    
    @Override
    public void close() {
        nonKeyedPartitioner.close();
    }

    @Override
    public void onNewBatch(String topic, Cluster cluster, int prevPartition) {
        nonKeyedPartitioner.onNewBatch(topic, cluster, prevPartition);
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        if (keyBytes == null) {
            return nonKeyedPartitioner.partition(topic, key, keyBytes, value, valueBytes, cluster);
        }
        final List<PartitionInfo> partitions = nonKeyedPartitioner.getPartitionCache().getPartitions(topic, cluster, true);
        // hash the keyBytes to choose a partition
        int partition = Utils.toPositive(Utils.murmur2(keyBytes)) % partitions.size();
        return partitions.get(partition).partition();
    }

    @Override
    public boolean isRackAware() {
        return nonKeyedPartitioner.isRackAware();
    }
    
}
