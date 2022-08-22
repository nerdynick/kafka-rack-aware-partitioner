# Apache Kafka Java Producer Rack Aware Partitioner

A collection Apache Kafka Java Producer Partitioners that extend the support for Rack Awareness to the Producer.

# How does it work?
For **non-keyed records**, the traditional Round-Robin approach of the Default Partitioner is used. 
It just filters what partitions are in the mix based on if the partitions current leader broker is within the selected rack.

For **keyed records**, the same logic is used as with the Default Partitioner. 
The difference is that the Prefered Leader for each partition is evaluated to determin which partitions the record can be written to.
Using the Prefered Leader allows for a more consitent hash during period of outages at the broker level. 
Keeping an outage from changing the resulting partition table and staying the expected outcome as you'd expect from the Default Partitioner.
