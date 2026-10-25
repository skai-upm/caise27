package com.amusementpark.persistence;

import com.amusementpark.business.model.Group;
import com.amusementpark.business.model.User;

import java.io.IOException;
import java.util.List;

/**
 * Storage of groups; members are resolved against the given users.
 */
public interface GroupRepository {

    List<Group> load(List<User> users) throws IOException;

    void save(List<Group> groups) throws IOException;
}
