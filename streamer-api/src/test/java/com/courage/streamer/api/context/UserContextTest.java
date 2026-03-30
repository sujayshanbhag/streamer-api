package com.courage.streamer.api.context;

import com.courage.streamer.common.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void setAndGetCurrentUserReturnsCorrectUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        UserContext.setCurrentUser(user);

        User retrieved = UserContext.getCurrentUser();
        assertNotNull(retrieved);
        assertEquals(1L, retrieved.getId());
        assertEquals("user@example.com", retrieved.getEmail());
    }

    @Test
    void getCurrentUserReturnsNullWhenNotSet() {
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void clearRemovesCurrentUser() {
        User user = new User();
        user.setId(2L);
        UserContext.setCurrentUser(user);

        UserContext.clear();

        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void setCurrentUserOverwritesPreviousUser() {
        User first = new User();
        first.setId(1L);
        User second = new User();
        second.setId(2L);

        UserContext.setCurrentUser(first);
        UserContext.setCurrentUser(second);

        assertEquals(2L, UserContext.getCurrentUser().getId());
    }
}
