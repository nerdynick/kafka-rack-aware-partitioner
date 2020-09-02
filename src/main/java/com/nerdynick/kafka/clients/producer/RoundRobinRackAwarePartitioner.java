package com.nerdynick.kafka.clients.producer;

import org.apache.kafka.common.Cluster;

public class RoundRobinRackAwarePartitioner extends RackAwarePartitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }
    
}
