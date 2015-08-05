package com.hamstercoders.netty.status;

public class RedirectRequestStatus {
    private long id;
    private String srcIp;
    private String redirectUrl;
    private long redirectCount;

    public RedirectRequestStatus(String redirectUrl, String srcIp) {
        this.redirectUrl = redirectUrl;
        this.srcIp = srcIp;
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

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public long getRedirectCount() {
        return redirectCount;
    }

    public void setRedirectCount(long redirectCount) {
        this.redirectCount = redirectCount;
    }
}
