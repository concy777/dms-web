package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RequestMessageInfo{
    @JsonProperty("BankId") 
    public String bankId;
    @JsonProperty("TimeZone") 
    public String timeZone;
    @JsonProperty("MessageDateTime") 
    public String messageDateTime;
}
