package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Token{
    @JsonProperty("Certificate") 
    public String certificate;
    @JsonProperty("MessageHashKey") 
    public String messageHashKey;
    @JsonProperty("MessageIndex") 
    public String messageIndex;
    @JsonProperty("PasswordToken") 
    public PasswordToken passwordToken;
}
