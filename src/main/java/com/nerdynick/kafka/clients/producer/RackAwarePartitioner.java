package com.nerdynick.kafka.clients.producer;

import org.apache.kafka.clients.producer.Partitioner;

public interface RackAwarePartitioner extends Partitioner {
    public boolean isRackAware();
}
