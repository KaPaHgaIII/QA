package ru.kapahgaiii.qa.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder builder) throws Exception {
        builder.inMemoryAuthentication().withUser("Dazar").password("951159").roles("STUDENT");
        builder.inMemoryAuthentication().withUser("Денис").password("951159").roles("STUDENT");
        builder.inMemoryAuthentication().withUser("Vladimir").password("1234").roles("STUDENT");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .antMatchers("/login").permitAll()
//                .antMatchers("/**").access("hasRole('ROLE_STUDENT')")
//                .and()
//                .formLogin().loginPage("/login").failureUrl("/login?error").defaultSuccessUrl("/")
//                .usernameParameter("username").passwordParameter("password")
//                .and()
//                .logout().logoutSuccessUrl("/")
//                .and()
//                .exceptionHandling().accessDeniedPage("/403")
//                .and()
//                .csrf();
        http.authorizeRequests()
                .antMatchers("/**").permitAll()
                .and()
                .formLogin().loginPage("/login")
                .successHandler(loginSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .and()
                .logout().logoutSuccessHandler(logoutSuccessHandler())
                .and()
                .csrf();
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler loginSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setUseReferer(true);
        return handler;
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
        handler.setUseReferer(true);
        return handler;
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
}


class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static Pattern p = Pattern.compile("^[^:]+://[^/]+(/.*)$");

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        Matcher m = p.matcher(request.getHeader("referer"));

        if (m.find()) {
            String url = m.group(1);
            response.sendRedirect(url + "#loginError");
            return;
        }
        response.sendRedirect("/#loginError");
    }
}