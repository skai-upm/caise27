package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.service.AttractionManager;
import es.upm.etsisi.amusementpark.business.service.GroupManager;
import es.upm.etsisi.amusementpark.business.service.PassManager;
import es.upm.etsisi.amusementpark.business.service.TicketManager;
import es.upm.etsisi.amusementpark.business.service.UserManager;

/** The business services created at startup and injected into the {@link ParkController}. */
public record ParkServices(UserManager users, GroupManager groups, TicketManager tickets,
                           AttractionManager attractions, PassManager passes) {
}
