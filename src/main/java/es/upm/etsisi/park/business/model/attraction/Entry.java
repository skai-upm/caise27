package es.upm.etsisi.park.business.model.attraction;

import java.util.List;

import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.user.User;

/** Result of a successful entry: who went in and which passes were consumed. */
public record Entry(List<User> users, List<Pass> consumedPasses) {

    public Entry {
        users = List.copyOf(users);
        consumedPasses = List.copyOf(consumedPasses);
    }
}
