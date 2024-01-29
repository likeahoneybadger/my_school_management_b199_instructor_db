package com.project.schoolmanagment.security.service;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long Id;

    private String username;

    private String name;

    private Boolean isAdvisor;

    @JsonIgnore
    private String password;

    private String ssn;
    //  We upgrade the user to GrantedAuthority
    private List<GrantedAuthority> authorities; //  it is the same as below one.
//    private Collection<? extends GrantedAuthority> authorities; //  a kind of collection extends Granted Authority class

    public UserDetailsImpl(Long id, String username, String name, Boolean isAdvisor,
                           String password, String ssn, String role /*  role info comes from DB as String so we changed authorities to role and GrantedAuthority to String*/) {
        Id = id;
        this.username = username;
        this.name = name;
        this.isAdvisor = isAdvisor;
        this.password = password;
        this.ssn = ssn;
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(role));
        this.authorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
