package com.amusementpark.persistence;

import com.amusementpark.business.model.User;

import java.util.List;

/**
 * Reference resolution shared by the repositories when restoring stored links.
 */
final class References {

    private References() {
    }

    static User findUser(List<User> users, String dni) {
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                return user;
            }
        }
        return null;
    }

    /**
     * The registered user with that DNI or, for the history of a deleted user, a detached user
     * that only knows its DNI.
     */
    static User userOrDetached(List<User> users, String dni) {
        User user = findUser(users, dni);
        return user != null ? user : new User("", "", dni, "", "", "", "");
    }
}
