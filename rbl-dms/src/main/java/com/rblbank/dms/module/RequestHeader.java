package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RequestHeader{
    @JsonProperty("MessageKey") 
    public MessageKey messageKey;
    @JsonProperty("RequestMessageInfo") 
    public RequestMessageInfo requestMessageInfo;
    @JsonProperty("Security") 
    public Security security;
    @JsonProperty("DeviceInfo") 
    public DeviceInfo deviceInfo;
    @JsonProperty("AdditionalInfo") 
    public AdditionalInfo additionalInfo;
}