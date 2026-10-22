package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.User;

import java.util.List;

/**
 * User groups.
 */
public interface GroupRepository extends Repository<Group> {

    List<Group> findByMember(User user);
}
