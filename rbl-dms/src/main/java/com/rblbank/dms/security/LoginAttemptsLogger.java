package com.rblbank.dms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;



@Component
public class LoginAttemptsLogger {

	private static final Logger logger = LoggerFactory.getLogger(LoginAttemptsLogger.class);
    @EventListener
    public void auditEventHappened(
      AuditApplicationEvent auditApplicationEvent) {
        
        AuditEvent auditEvent = auditApplicationEvent.getAuditEvent();
        System.out.println("Principal " + auditEvent.getPrincipal() 
          + " - " + auditEvent.getType());
        
        logger.info("Principal " + auditEvent.getPrincipal() 
          + " - " + auditEvent.getType());

        WebAuthenticationDetails details = 
          (WebAuthenticationDetails) auditEvent.getData().get("details");
        System.out.println("Remote IP address: " 
          + details.getRemoteAddress());
        logger.info("Remote IP address: " 
          + details.getRemoteAddress());
        System.out.println("  Session Id: " + details.getSessionId());
        System.out.println("  Request URL: " 
                + auditEvent.getData().get("requestUrl"));
       logger.info("  Request URL: " 
                + auditEvent.getData().get("requestUrl"));
    }
}
