package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


@Data
public class AdditionalInfo{
    @JsonProperty("SessionId") 
    public String sessionId;
    @JsonProperty("LanguageId") 
    public String languageId;
    @JsonProperty("JourneyId") 
    public String journeyId;
    @JsonProperty("SVersion") 
    public String sVersion;
    
}
