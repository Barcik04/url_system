//package com.example.url_system.security;
//
//
//import com.example.url_system.models.User;
//import com.example.url_system.repositories.UserRepository;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//    private final UserRepository repo;
//
//    public CustomUserDetailsService(UserRepository repo) { this.repo = repo; }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User u = repo.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
//        return new org.springframework.security.core.userdetails.User(
//                u.getUsername(),
//                u.getPassword(),
//                u.getRole().getAuthorities()
//        );
//    }
//}
