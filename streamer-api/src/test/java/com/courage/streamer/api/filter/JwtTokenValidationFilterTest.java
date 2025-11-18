package com.courage.streamer.api.filter;

import com.courage.streamer.api.config.BypassUrlsConfig;
import com.courage.streamer.api.filter.JwtTokenValidationFilter;
import com.courage.streamer.api.service.UserAuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenValidationFilterTest {

    private JwtTokenValidationFilter filter;
    private UserAuthorizationService userAuthorizationService;
    private BypassUrlsConfig bypassUrlsConfig;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        userAuthorizationService = mock(UserAuthorizationService.class);
        bypassUrlsConfig = mock(BypassUrlsConfig.class);
        filter = new JwtTokenValidationFilter(userAuthorizationService, bypassUrlsConfig);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldNotFilterReturnsTrueForBypassUrls() {
        when(bypassUrlsConfig.getUrls()).thenReturn(List.of("/auth/signup", "/auth/login"));
        when(request.getRequestURI()).thenReturn("/auth/signup");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterReturnsFalseForNonBypassUrls() {
        when(bypassUrlsConfig.getUrls()).thenReturn(List.of("/auth/signup", "/auth/login"));
        when(request.getRequestURI()).thenReturn("/api/resource");

        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternalAuthorizesWhenValidationReturnsTrue() throws ServletException, IOException {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("1");
        when(jwt.getClaimAsString("ver")).thenReturn("2");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userAuthorizationService.validate(1L, "2")).thenReturn(true);

        when(request.getRequestURI()).thenReturn("/api/resource");

        filter.doFilterInternal(request, response, filterChain);

        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalSetsUnauthorizedWhenValidationFails() throws ServletException, IOException {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("1");
        when(jwt.getClaimAsString("ver")).thenReturn("9");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userAuthorizationService.validate(1L, "9")).thenReturn(false);

        when(request.getRequestURI()).thenReturn("/api/resource");
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Unauthorized - token invalid");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternalSetsUnauthorizedWhenAuthenticationIsNull() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(null);
        when(request.getRequestURI()).thenReturn("/api/resource");
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Unauthorized - token invalid");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternalSetsUnauthorizedWhenPrincipalIsNotJwt() throws ServletException, IOException {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("nonJwtPrincipal");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(request.getRequestURI()).thenReturn("/api/resource");
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write("Unauthorized - token invalid");
        verify(filterChain, never()).doFilter(any(), any());
    }
}