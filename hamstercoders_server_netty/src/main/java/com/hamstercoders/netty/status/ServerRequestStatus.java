package com.hamstercoders.netty.status;

import java.sql.Timestamp;
import java.util.Calendar;

public class ServerRequestStatus {

    private String srcIp;
    private Timestamp lastRequest;
    private long requestCount;

    public ServerRequestStatus(String srcIp) {
        super();
        this.srcIp = srcIp;
        this.lastRequest = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public ServerRequestStatus(String srcIp, Timestamp lastRequest) {
        this.srcIp = srcIp;
        this.lastRequest = lastRequest;
    }

    public ServerRequestStatus(String srcIp, Timestamp lastRequest, long requestCount) {
        this.srcIp = srcIp;
        this.lastRequest = lastRequest;
        this.requestCount = requestCount;
    }

    public void incRequestCount() {
        requestCount++;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public Timestamp getLastRequest() {
        return lastRequest;
    }

    public void setLastRequest(Timestamp lastRequest) {
        this.lastRequest = lastRequest;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }
}
