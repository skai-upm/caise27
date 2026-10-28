package es.upm.amusementpark.business;

import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.persistence.file.DataLocation;
import es.upm.amusementpark.persistence.file.ParkStorage;
import es.upm.amusementpark.persistence.file.UserFileStorage;
import es.upm.amusementpark.persistence.file.UserStorage;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Composition root: creates the repositories and storages of an execution and injects them, with
 * the collaborations between controllers, into the singleton controllers.
 */
final class ParkInitializer {

    private ParkInitializer() {
    }

    /**
     * New in-memory repositories; users optionally backed by the given storage.
     */
    static ParkDependencies inMemory(UserStorage userStorage) {
        return new ParkDependencies(new UserRepository(), new GroupRepository(), new TicketRepository(),
                new AttractionRepository(), new PassRepository(), userStorage, null);
    }

    /**
     * New repositories backed by the data files of the given location (permanent or temporary).
     */
    static ParkDependencies forLocation(DataLocation location) {
        return new ParkDependencies(new UserRepository(), new GroupRepository(), new TicketRepository(),
                new AttractionRepository(), new PassRepository(), new UserFileStorage(location.users()),
                ParkStorage.at(location));
    }

    static void wire(ParkController park, ParkDependencies deps) {
        UserService users = UserService.getInstance().configure(deps.users(), deps.userStorage());
        TicketService tickets = TicketService.getInstance().configure(deps.tickets(), users, park::getCurrentDate,
                park::getBasePrice);
        GroupService groups = GroupService.getInstance().configure(deps.groups(), users, tickets::hasActiveGroupTicket,
                park::getCurrentDate);
        AttractionService attractions = AttractionService.getInstance().configure(deps.attractions());
        PassService passes = PassService.getInstance().configure(deps.passes(), tickets, park::getCurrentDate);
        InvoiceService invoices = InvoiceService.getInstance().configure(users, groups, tickets, passes);
        park.configure(deps, users, groups, tickets, attractions, invoices, passes);
    }
}
