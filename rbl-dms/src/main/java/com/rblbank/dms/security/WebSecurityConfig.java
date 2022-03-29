package com.rblbank.dms.security;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.authentication.ProviderManager;

import com.rblbank.dms.security.jwt.AuthEntryPointJwt;
import com.rblbank.dms.security.jwt.AuthTokenFilter;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@EnableEncryptableProperties
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    // securedEnabled = true,
    // jsr250Enabled = true,
    prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  //@Autowired
  //UserDetailsServiceImpl userDetailsService;
	private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;
  
  @Value("${dms-ldap-url}")
  private String ldapUrls;
  
  @Value("${dms-base-dn}")
  private String ldapBaseDn;
  
  @Value("${ldap.enabled}")
  private String ldapEnabled;
  
  @Value("${dms.hostname}")
  private String dms_hostname;
  
  @Value("${dms.linux.hostname}")
  private String dms_linux_hostname;
  

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }
  
  @Bean
  public CustomAuthenticationProvider authProvider() {
      CustomAuthenticationProvider authenticationProvider = new CustomAuthenticationProvider();
      return authenticationProvider;
  }

  @Override
  public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
	  authenticationManagerBuilder.authenticationProvider(authProvider());
	 // authenticationManagerBuilder.authenticationProvider(activeDirectoryLdapAuthenticationProvider()).userDetailsService(userDetailsService());
	  
		/*
		 * authenticationManagerBuilder .ldapAuthentication() .contextSource()
		 * .url(ldapUrls + ldapBaseDn) .managerDn(ldapSecurityPrincipal)
		 * .managerPassword(ldapPrincipalPassword) .and()
		 * .userDnPatterns(ldapUserDnPattern);
		 */
		
		/*
		 * if(Boolean.parseBoolean(ldapEnabled)) { authenticationManagerBuilder
		 * .ldapAuthentication() .contextSource() .url(ldapUrls + ldapBaseDn)
		 * .managerDn(ldapSecurityPrincipal) .managerPassword(ldapPrincipalPassword)
		 * .and() .userDnPatterns(ldapUserDnPattern); } else {
		 * 
		 * authenticationManagerBuilder.authenticationProvider(authProvider()); }
		 */
		 
		  
	 //  authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  }
  
	/*
	 * @Bean public AuthenticationManager authenticationManager() { return new
	 * ProviderManager(Arrays.asList(activeDirectoryLdapAuthenticationProvider()));
	 * }
	 * 
	 * @Bean public AuthenticationProvider
	 * activeDirectoryLdapAuthenticationProvider() {
	 * ActiveDirectoryLdapAuthenticationProvider provider = new
	 * ActiveDirectoryLdapAuthenticationProvider(AD_DOMAIN, AD_URL);
	 * provider.setConvertSubErrorCodesToExceptions(true);
	 * provider.setUseAuthenticationRequestCredentials(true);
	 * 
	 * return provider; }
	 */

	
	  @Bean
	  @Override public AuthenticationManager authenticationManagerBean() throws Exception { 
		  return super.authenticationManagerBean(); 
		  }
	 

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable()
      .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
      .authorizeRequests().antMatchers("/api/auth/**","/login","/logoutPage","/accessDenied","/expired","/dms-app/**").permitAll()
      .antMatchers("/api/test/**").permitAll()
      .antMatchers( "/favicon.ico").permitAll()
      .antMatchers("/dms-app/**").permitAll()
      .antMatchers("/**").hasAnyRole("ADMIN","USER","SUPER_ADMIN")
      .anyRequest().authenticated()
      .and().formLogin().loginPage("/login").usernameParameter("username")
 	  .passwordParameter("password").permitAll()
 	  .loginProcessingUrl("/loginApp")
 	  .successForwardUrl("/dms-app/login-app") 
 	  .failureHandler(new AuthenticationFailureHandler() {
 		 
          @Override
          public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                  AuthenticationException exception) throws IOException, ServletException {
              String username = request.getParameter("username");
              String error = exception.getMessage();
              System.out.println("A failed login attempt with username: "
                                  + username + ". Reason: " + error);
              logger.info("A failed login attempt with username: "
                                  + username + ". Reason: " + error);

              String redirectUrl = request.getContextPath() + "/login?error=true";
              response.sendRedirect(redirectUrl);
          }
      })
 	  .and() 
 	  .logout()
 	 .logoutSuccessHandler(new LogoutSuccessHandler() {
 		 
         @Override
         public void onLogoutSuccess(HttpServletRequest request,
                     HttpServletResponse response, Authentication authentication)
                 throws IOException, ServletException {
             
             String username = authentication.getName();

             System.out.println("The user " + username + " has logged out.");
             logger.info("The user " + username + " has logged out successfully.");

             response.sendRedirect(request.getContextPath()+ "/logoutPage");
         }
     })
 	 .logoutUrl("/logout")  
 	 
     .logoutSuccessUrl("/logoutPage")
	 .invalidateHttpSession(true) 
	 .deleteCookies("CUSTOMSESSIONID")
	 .and()
     .exceptionHandling()
         .accessDeniedPage("/accessDenied");
	  
	  http.sessionManagement()
	  .sessionFixation().migrateSession()
      .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
      .invalidSessionUrl("/login?invalid-session=true")
      .maximumSessions(1)
      .maxSessionsPreventsLogin(true)
      .expiredUrl("/login?invalid-session=true");
	 
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
  }
  
  @Bean
  public SessionRegistry sessionRegistry() {
      SessionRegistry sessionRegistry = new SessionRegistryImpl();
      return sessionRegistry;
  }
  
  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
      return new HttpSessionEventPublisher();
  }
  
  @Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowedHostnames(hostname -> hostname.equals(dms_hostname) || hostname.equals(dms_linux_hostname));
		firewall.setAllowUrlEncodedSlash(true);
		firewall.setAllowSemicolon(true);
		return firewall;
	}

  
  @Override
	public void configure(WebSecurity web) throws Exception {
		// Allow swagger to be accessed without authentication

		web.httpFirewall(allowUrlEncodedSlashHttpFirewall());

		web.ignoring().antMatchers("/dms-app/**")//
				.antMatchers("/swagger-resources/**")//
				.antMatchers("/swagger-ui.html")//
				.antMatchers("/configuration/**")//
				.antMatchers("/webjars/**")//
				.antMatchers("/public")
	            .antMatchers("/resources/**", "/static/**", "/css/**","/dms-app/css/**", "/js/**", "/img/**", "/icon/**","/font-awesome/**")
				// Un-secure H2 Database (for testing purposes, H2 console shouldn't be
				// unprotected in production)
				.and().ignoring().antMatchers("/h2-console/**/**");
		;
	}
}
