package com.project.schoolmanagment.security.jwt;

import com.project.schoolmanagment.security.service.UserDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenFilter.class);

    private JwtUtils jwtUtils;
    private UserDetailServiceImpl userDetailService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //  1) From every request we will get JWT
            String jwt = parseJwt(request);
            //  2) validate JWT
            if(jwt != null && jwtUtils.validateJwtToken(jwt)){
            //  3) we need username for the get data
                String userName = jwtUtils.getUsernameFromJwtToken(jwt);
            //  4) check DB find the user and upgrade it with userDetails
                UserDetails userDetails = userDetailService.loadUserByUsername(userName);
            //  5) We are setting attribute prop with username.
                request.setAttribute("username",userName);
            //  6) we have userdetails object then we have to send this information to
            //      SECURITY CONTEXT
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //  7) now spring context know who is logged in.
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }catch (UsernameNotFoundException error){
            LOGGER.error("Cannot set user authentication: "+error);
        }
        filterChain.doFilter(request,response);
    }

    //  If it starts with Bearer fgdfgdfgdfhdfhfdghdfhsdfsdfsdfsd23rdsfdgdgdf or give me the beginning 7 chars.
    private String parseJwt (HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }
}
