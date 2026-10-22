package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.Group;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.GroupRepository;
import es.upm.etsisi.park.persistence.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups stored in a text file through a {@link GroupFileStore}.
 */
public class FileGroupRepository extends ListRepository<Group> implements GroupRepository {

    private final GroupFileStore store;

    public FileGroupRepository(GroupFileStore store, UserRepository users) {
        super(store.load(users::findByDni), Group::getId);
        this.store = store;
    }

    @Override
    protected void persist(List<Group> current) {
        store.save(current);
    }

    @Override
    public List<Group> findByMember(User user) {
        List<Group> result = new ArrayList<>();
        for (Group group : items) {
            if (group.contains(user)) {
                result.add(group);
            }
        }
        return result;
    }
}
