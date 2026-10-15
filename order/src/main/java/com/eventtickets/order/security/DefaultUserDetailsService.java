package com.eventtickets.order.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("defaultUserDetailsService")
public class DefaultUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("load user by username {}", username);

        // TODO: CALL DB TO GET USER PERMISSIONS
        if (username.equals("invaliduser")) {
            throw new UsernameNotFoundException("Invalid user");
        }
        return new DefaultUserDetails(username, List.of());
    }
}
