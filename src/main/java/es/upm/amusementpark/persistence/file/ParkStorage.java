package es.upm.amusementpark.persistence.file;

/**
 * File storages of the park elements other than users, all configured for one data location.
 */
public record ParkStorage(GroupFileStorage groups, TicketFileStorage tickets, AttractionFileStorage attractions) {

    public static ParkStorage at(DataLocation location) {
        return new ParkStorage(new GroupFileStorage(location.groups()), new TicketFileStorage(location.tickets()),
                new AttractionFileStorage(location.attractions()));
    }
}
