//package com.example.url_system.models;
//
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.Collection;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public enum Role {
//    USER(Set.of(Permission.USER)),
//    ADMIN(Set.of(
//            Permission.USER,
//            Permission.ADMIN
//    ));
//
//    private final Set<Permission> permissions;
//
//    Role(Set<Permission> permissions) {
//        this.permissions = permissions;
//    }
//
//    public Set<Permission> getPermissions() {
//        return permissions;
//    }
//
//    /** What Spring Security will see on Authentication.getAuthorities() */
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // expose permissions as plain authorities, e.g. "USER_READ"
//        return permissions.stream()
//                .map(p -> new SimpleGrantedAuthority(p.name()))
//                .collect(Collectors.toUnmodifiableSet());
//
//    }
//}
