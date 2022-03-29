package com.rblbank.dms.security;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class Ldap {
     public static void main(String[] args) {
    	 
    	// service user
    	    String serviceUserDN = "25766@ratnakarbank.in";
    	    String serviceUserPassword = "Kutty@2023";
    	 
          Hashtable<String, String> env = new Hashtable<String, String>();
          env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
          env.put(Context.PROVIDER_URL,"ldaps://ldaps.ratnakarbank.in:636");
          //env.put(Context.SECURITY_AUTHENTICATION, "simple");
          env.put(Context.SECURITY_PRINCIPAL, serviceUserDN);
          env.put(Context.SECURITY_CREDENTIALS, serviceUserPassword);
          env.put("java.naming.ldap.factory.socket", "MySSLSocketFactory");
          
          
          try {
               //bind to the domain controller
               LdapContext ctx = new InitialLdapContext(env,null);
              ctx = new InitialLdapContext(env, null);
               
               System.out.println("LDAP Connection Successful");
               System.exit(0);
          } catch (NamingException e) {
               System.err.println("LDAP Notifications failure. " + e);
               System.exit(1);
          }

      }
}


