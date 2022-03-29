package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class MessageKey{
    @JsonProperty("RequestUUID") 
    public String requestUUID;
    @JsonProperty("ServiceRequestId") 
    public String serviceRequestId;
    @JsonProperty("ServiceRequestVersion") 
    public String serviceRequestVersion;
    @JsonProperty("ChannelId") 
    public String channelId;
}
