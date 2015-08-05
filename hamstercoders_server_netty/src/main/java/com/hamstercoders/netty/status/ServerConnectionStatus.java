package com.hamstercoders.netty.status;

import java.sql.Timestamp;
import java.util.Calendar;

public class ServerConnectionStatus {

    private long id;
    private String srcIp;
    private String uri;
    private Timestamp timestamp;
    private long sentBytes;
    private long receivedBytes;
    private long throughput;


    public ServerConnectionStatus() {
        setId(0);
        setSrcIp(null);
        setUri(null);
        setTimestamp(new Timestamp(Calendar.getInstance().getTimeInMillis()));
        setSentBytes(0);
        setReceivedBytes(0);
        setThroughput(0);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public long getSentBytes() {
        return sentBytes;
    }

    public void setSentBytes(long sentBytes) {
        this.sentBytes = sentBytes;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public void setReceivedBytes(long receivedBytes) {
        this.receivedBytes = receivedBytes;
    }

    public long getThroughput() {
        return throughput;
    }

    public void setThroughput(long throughput) {
        this.throughput = throughput;
    }
}
