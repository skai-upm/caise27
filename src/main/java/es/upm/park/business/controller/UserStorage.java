package es.upm.park.business.controller;

import es.upm.park.business.model.User;

import java.util.List;

/** Where users are kept between executions. */
public interface UserStorage {

    List<User> load();

    void save(List<User> users);

    /** Storage that keeps nothing: the default when no persistence is configured. */
    UserStorage NONE = new UserStorage() {
        @Override
        public List<User> load() {
            return List.of();
        }

        @Override
        public void save(List<User> users) {
            // nothing to keep
        }
    };
}
