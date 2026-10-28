package es.upm.amusementpark.service;

import java.time.LocalDate;

/**
 * Tells whether a group has a group ticket dated on or after the given day. A group with such a
 * ticket is locked.
 */
@FunctionalInterface
public interface ActiveGroupTicketCheck {

    boolean hasActiveGroupTicket(String groupId, LocalDate today);
}
