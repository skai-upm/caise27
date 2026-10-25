package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Something that gives access to attractions: a ticket for a date that lets in one or more users.
 */
public interface AccessCapability {

    String getId();

    LocalDate getDate();

    /**
     * The users this access lets in.
     */
    List<User> getUsers();

    /**
     * The individual accesses that make up this access, one per user; their passes are used at entry.
     */
    List<? extends IndividualAccess> getIndividualAccesses();
}
