package es.upm.etsisi.park;

import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.GeneralAccess;
import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.Ticket;
import es.upm.etsisi.park.business.User;
import es.upm.etsisi.park.persistence.TextParkRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/** Loads normally but fails every save, to check that commands are undone. */
class FailingRepository extends TextParkRepository {

    FailingRepository(Path directory) {
        super(directory);
    }

    @Override
    public void saveAll(Collection<User> users, Collection<Group> groups, Collection<Ticket> tickets,
                        Collection<Attraction<? extends GeneralAccess>> attractions, Collection<Pass> passes)
            throws IOException {
        throw new IOException("disk full");
    }
}
