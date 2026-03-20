package com.courage.streamer.api.context;

import com.courage.streamer.api.model.entity.User;

public class UserContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    private UserContext() {}

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
