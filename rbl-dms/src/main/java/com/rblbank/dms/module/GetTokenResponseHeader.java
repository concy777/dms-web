package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class GetTokenResponseHeader{
    @JsonProperty("MessageKey") 
    public MessageKey messageKey;
    @JsonProperty("ResponseMessageInfo") 
    public ResponseMessageInfo responseMessageInfo;
    @JsonProperty("AdditionalInfo") 
    public GetTokenAdditionalInfo additionalInfo;
}