package com.project.schoolmanagment.security.config;

import com.project.schoolmanagment.security.jwt.AuthEntryPointJwt;
import com.project.schoolmanagment.security.jwt.AuthTokenFilter;
import com.project.schoolmanagment.security.service.UserDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailServiceImpl userDetailService;

    private final AuthEntryPointJwt authEntryPointJwt;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        //  CORS (cross origin resource sharing) and CSRF (Cross-Site Request Forgery) is disabled.
        http.cors().and()
                .csrf().disable()
        //  Configure the exception handler
                .exceptionHandling().authenticationEntryPoint(authEntryPointJwt).and()
        //  Configure the session management
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        //  Configure white list
                .authorizeRequests().antMatchers(AUTH_WHITE_LIST).permitAll()
        //  Configure authentication all rest of the URL.s
                .anyRequest().authenticated();
        //  Configure the frames to be sure from the same origin
        http.headers().frameOptions().sameOrigin();
        //  Configure the authentication provider
        http.authenticationProvider(daoAuthenticationProvider());
        //  Configure JWT token handling
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(){
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration configuration)
        throws Exception{
            return configuration.getAuthenticationManager();
        }
        @Bean
        public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
        }
        @Bean
        public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
        }

    @Bean
    public WebMvcConfigurer corsConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        //  we let all sources to call our API.s
                        .allowedOrigins("*")
                        //  we let all header properties
                        .allowedHeaders("*")
                        //  we allow (GET, POST, PUT, PATCH, DELETE...requests)
                        .allowedMethods("*");
            }
        };
    }
    private static final String[] AUTH_WHITE_LIST = {
            "/v3/api-docs/**", // eklenecek
            "swagger-ui.html", // eklenecek
            "/swagger-ui/**", // eklenecek
            "/",
            "index.html",
            "/images/**",
            "/css/**",
            "/js/**",
            "/contactMessages/save",
            "/auth/login"
    };

}
