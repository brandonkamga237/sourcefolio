package com.brandonkamga.sourcefolio.account.infra.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * What the session keeps about a signed-in member: our id and the login to display, not the
 * GitHub payload. It is serialised into the session table, so it stays small and stable.
 * getName() is the member id: controllers read it without knowing this class.
 */
record MemberPrincipal(UUID userId, String login) implements OAuth2User, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of("id", userId.toString(), "login", login);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
