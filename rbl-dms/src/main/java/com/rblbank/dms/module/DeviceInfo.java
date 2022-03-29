package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DeviceInfo{
    @JsonProperty("DeviceFamily") 
    public String deviceFamily;
    @JsonProperty("DeviceFormat") 
    public String deviceFormat;
    @JsonProperty("DeviceType") 
    public String deviceType;
    @JsonProperty("DeviceName") 
    public String deviceName;
    @JsonProperty("DeviceIMEI") 
    public String deviceIMEI;
    @JsonProperty("DeviceID") 
    public String deviceID;
    @JsonProperty("DeviceVersion") 
    public String deviceVersion;
    @JsonProperty("AppVersion") 
    public String appVersion;
    @JsonProperty("DeviceOS") 
    public String deviceOS;
    @JsonProperty("DeviceIp") 
    public String deviceIp;
}