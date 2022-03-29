package com.rblbank.dms.security;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import com.rblbank.dms.models.User;
import com.rblbank.dms.repository.UserRepository;
import com.rblbank.dms.security.services.UserDetailsImpl;

public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	UserRepository userRepository;
	
	@Value("${dms-ldap-url}")
	  private String ldapUrls;
	
	@Value("${dms-base-dn}")
	  private String ldapBaseDn;
	
	@Value("${ldap.enabled}")
	  private String ldapEnabled;
	
	
    private LdapContextSource contextSource;
    private LdapTemplate ldapTemplate;
    private void initContext()
    {  
        contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrls);
        contextSource.setAnonymousReadOnly(true);
       // contextSource.setUserDn("uid={0},ou=people");
        contextSource.afterPropertiesSet();

        ldapTemplate = new LdapTemplate(contextSource);
    }
    
    public void verifyCredentials(String userId,
	        String password) {
		System.out.println("inside verifyCredentials() userId="
		        + userId + "::password=" + password);
		LdapQuery query = LdapQueryBuilder.query().where("uid")
		        .is(userId);

		ldapTemplate.authenticate(query, password);
	}
    
    public String verifyLdap(String userId,
	        String password) {
    	
		
		Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,ldapUrls);
        //env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userId);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put("java.naming.ldap.factory.socket", "com.rblbank.dms.security.SSLFactory");
        
         
        
        
        try {
             //bind to the domain controller
             LdapContext ctx = new InitialLdapContext(env,null);
            ctx = new InitialLdapContext(env, null);
             
             System.out.println("LDAP Connection Successful");
            return "success";
        } catch (NamingException e) {
             System.err.println("LDAP Notifications failure. " + e);
            return e.getMessage();
        }
        catch (AuthenticationException e) {
            System.err.println("Bad Credential. " + e);
           return "Bad Credential";
       }
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		System.out.println("ldapUrls-----"+ldapUrls);
		System.out.println("ldapBaseDn-----"+ldapBaseDn);
		if(Boolean.parseBoolean(ldapEnabled)) {
		String authenticate=	verifyLdap(authentication.getName()+"@ratnakarbank.in", authentication.getCredentials().toString());
			/*
			 * initContext(); Filter filter = new EqualsFilter("uid",
			 * authentication.getName()+"@ratnakarbank.in");
			 * ldapTemplate.setIgnorePartialResultException(true); Boolean authenticate =
			 * ldapTemplate.authenticate(ldapBaseDn, filter.encode(),
			 * authentication.getCredentials().toString());
			 */
			System.out.println("Ldap success"+authenticate);
	        if (authenticate.equalsIgnoreCase("success"))
	        {
	        	final String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
	    		if (StringUtils.isEmpty(username)) {
	    			throw new BadCredentialsException("invalid login details");
	    		}
	    		//User user = userRepository.findByUsername(username)
	    				//.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
	    		
	    		return new UsernamePasswordAuthenticationToken(
	    				username, authentication.getCredentials(), new ArrayList<>());
	    		
	        }
	        else
	        {
	        	throw new BadCredentialsException("Invalid login details");
	        }
		}
		else {
			final String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
    		if (StringUtils.isEmpty(username)) {
    			throw new BadCredentialsException("invalid login details");
    		}
    		//User user = userRepository.findByUsername(username)
    				//.orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
    		
    		return new UsernamePasswordAuthenticationToken(
    				username, authentication.getCredentials(), new ArrayList<>());
    		
		}
		
        
		
		//return createSuccessfulAuthentication(authentication, UserDetailsImpl.build(user));
	}

	/*
	 * private Authentication createSuccessfulAuthentication(final Authentication
	 * authentication, final UserDetails user) { UsernamePasswordAuthenticationToken
	 * token = new UsernamePasswordAuthenticationToken(user.getUsername(),
	 * user.getPassword(), user.getAuthorities());
	 * token.setDetails(authentication.getDetails()); return token; }
	 */

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
