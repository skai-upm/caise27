package es.upm.etsisi.park.business;

/**
 * The configured business entry points handed to the presentation layer.
 */
public record ParkServices(Park park, UserController users, GroupController groups, TicketController tickets,
                           PassController passes, AttractionController attractions, InvoiceController invoices) {
}
