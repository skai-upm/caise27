package es.upm.amusementpark.park;

import java.time.LocalDate;
import java.util.List;

/**
 * Dates of the group tickets associated with a group, provided by group ticket management.
 */
@FunctionalInterface
public interface GroupTicketDates {

    /** Provider used while no group tickets exist. */
    GroupTicketDates NONE = groupId -> List.of();

    List<LocalDate> datesOf(String groupId);
}
