package es.upm.amusementpark.presentation;

import java.io.IOException;
import java.time.LocalDate;

import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.AttractionFileRepository;
import es.upm.amusementpark.persistence.GroupFileRepository;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.TicketFileRepository;
import es.upm.amusementpark.persistence.UserFileRepository;

/**
 * Builds the application: creates the file repositories for the chosen storage
 * location and hands them to the business layer.
 */
public final class AppBootstrap {

    private AppBootstrap() {
    }

    public static ParkController create(StoragePaths paths, LocalDate startDate) throws IOException {
        return new ParkController(
                new UserFileRepository(paths.users()),
                new GroupFileRepository(paths.groups()),
                new TicketFileRepository(paths.tickets()),
                new AttractionFileRepository(paths.attractions()),
                startDate);
    }
}
