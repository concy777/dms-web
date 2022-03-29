package com.rblbank.dms.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PasswordToken{
    @JsonProperty("UserId") 
    public String userId;
    @JsonProperty("Password") 
    public String password;
}
