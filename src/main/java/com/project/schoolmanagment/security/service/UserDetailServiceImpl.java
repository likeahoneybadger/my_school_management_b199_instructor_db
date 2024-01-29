package com.project.schoolmanagment.security.service;

import com.project.schoolmanagment.entity.concretes.user.User;
import com.project.schoolmanagment.repository.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {   //  it will return UserDetails class as UserDetailServiceImpl
        User user = userRepository.findByUsername(username);
        //  we Upgraded the user to userdetails


        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getIsAdvisor(),
                user.getPassword(),
                user.getSsn(),
                user.getUserRole().getRoleType().getName()
        );
    }
}
