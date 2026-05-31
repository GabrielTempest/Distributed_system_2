package com.eoc.dashboard.model;

public class KafkaQueue {

    public enum QueueType { SENSOR, ALERT, INFRASTRUCTURE }

    private final String    topicPattern;
    private final String    description;
    private final QueueType queueType;
    private final String    partitionKey;
    private final boolean   cascade;

    public KafkaQueue(String topicPattern, String description,
                      QueueType queueType, String partitionKey, boolean cascade) {
        this.topicPattern = topicPattern;
        this.description  = description;
        this.queueType    = queueType;
        this.partitionKey = partitionKey;
        this.cascade      = cascade;
    }

    public String    getTopicPattern() { return topicPattern; }
    public String    getDescription()  { return description; }
    public QueueType getQueueType()    { return queueType; }
    public String    getPartitionKey() { return partitionKey; }
    public boolean   isCascade()       { return cascade; }

    public String getAccentColor() {
        return switch (queueType) {
            case SENSOR         -> "#56b6c2";
            case ALERT          -> "#e06c75";
            case INFRASTRUCTURE -> "#e5a840";
        };
    }
}
