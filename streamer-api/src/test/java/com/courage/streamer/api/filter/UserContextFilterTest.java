package com.courage.streamer.api.filter;

import com.courage.streamer.api.context.UserContext;
import com.courage.streamer.api.service.UserService;
import com.courage.streamer.common.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;

import static org.mockito.Mockito.*;

class UserContextFilterTest {

    private UserService userService;
    private UserContextFilter userContextFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userContextFilter = new UserContextFilter(userService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilterInternalSetsUserContextWhenAuthenticationIsValid() throws ServletException, IOException {
        // Mock JWT and Authentication
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("1");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock UserService
        User user = new User();
        user.setId(1L);
        when(userService.findById(1L)).thenReturn(user);

        // Mock UserContext
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Call the filter
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Verify Jwt.getSubject() was called
            verify(jwt).getSubject();

            // Verify UserService was called
            verify(userService).findById(1L);

            // Verify UserContext.setCurrentUser was called
            mockedUserContext.verify(() -> UserContext.setCurrentUser(user));

            // Verify the filter chain continues
            verify(filterChain).doFilter(request, response);

            // Verify UserContext.clear was called
            mockedUserContext.verify(UserContext::clear);
        }
    }

    @Test
    void doFilterInternalHandlesExceptionAndSetsErrorResponse() throws ServletException, IOException {
        // Mock JWT and Authentication
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("1");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock UserService to throw an exception
        when(userService.findById(1L)).thenThrow(new RuntimeException("User not found"));

        // Mock response writer
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // Mock UserContext
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Call the filter
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Verify error response
            verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            verify(writer).write("An error occurred while processing the user context.");

            // Verify the filter chain does not continue
            verify(filterChain, never()).doFilter(request, response);

            // Verify UserContext.clear was called
            mockedUserContext.verify(UserContext::clear);
        }
    }

    @Test
    void doFilterInternalClearsUserContextAfterExecution() throws ServletException, IOException {
        // Mock JWT and Authentication
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("1");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock UserService
        User user = new User();
        user.setId(1L);
        when(userService.findById(1L)).thenReturn(user);

        // Mock UserContext
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            // Call the filter
            userContextFilter.doFilterInternal(request, response, filterChain);

            // Verify UserContext.clear was called
            mockedUserContext.verify(UserContext::clear);
        }
    }
}