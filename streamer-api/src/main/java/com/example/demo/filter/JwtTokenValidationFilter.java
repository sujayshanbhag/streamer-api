package com.example.demo.filter;

import com.example.demo.config.BypassUrlsConfig;
import com.example.demo.service.UserAuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtTokenValidationFilter extends OncePerRequestFilter {
    private final UserAuthorizationService userAuthorizationService;
    private final BypassUrlsConfig bypassUrlsConfig;

    public JwtTokenValidationFilter(UserAuthorizationService userAuthorizationService, BypassUrlsConfig bypassUrlsConfig) {
        this.userAuthorizationService = userAuthorizationService;
        this.bypassUrlsConfig = bypassUrlsConfig;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return bypassUrlsConfig.getUrls().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isAuthorized = false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String username = jwt.getSubject();
            String version = jwt.getClaimAsString("ver");

            isAuthorized = userAuthorizationService.validate(Long.valueOf(username), version);
        }

        if (!isAuthorized) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized - token invalid");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
