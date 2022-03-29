package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class ResponseHeader{
    @JsonProperty("MessageKey") 
    public MessageKey messageKey;
    @JsonProperty("ResponseMessageInfo") 
    public ResponseMessageInfo responseMessageInfo;
    @JsonProperty("AdditionalInfo") 
    public AdditionalInfo additionalInfo;
}
