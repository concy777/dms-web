package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Security{
    @JsonProperty("Token") 
    public Token token;
}
