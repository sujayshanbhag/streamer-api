package com.courage.streamer.api.filter;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.common.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    private final UserService userService;

    public UserContextFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                Long userId = Long.valueOf(jwt.getSubject());
                User user = userService.findById(userId);
                UserContext.setCurrentUser(user);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing user context", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred while processing the user context.");
        } finally {
            UserContext.clear();
        }
    }
}
