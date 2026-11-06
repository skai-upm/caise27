package es.upm.park.business.service;

import java.util.List;

import es.upm.park.business.model.Pass;
import es.upm.park.business.model.User;

/**
 * Outcome of a successful attraction entry: who entered and which passes
 * were consumed.
 */
public record AccessResult(List<User> users, List<Pass> consumed) {

    public AccessResult {
        users = List.copyOf(users);
        consumed = List.copyOf(consumed);
    }
}
