package com.rblbank.dms.security.services;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password);
}
