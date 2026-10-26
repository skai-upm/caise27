package es.upm.parque.presentation;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;

/**
 * The business controllers the command processor works with, configured at startup.
 *
 * @param main        system date and price
 * @param users       user management
 * @param groups      group management
 * @param tickets     individual and group tickets
 * @param attractions attractions and access
 * @param invoices    invoice construction
 * @param passes      personal passes
 */
public record ParkControllers(MainController main, UserController users, GroupController groups,
                              TicketController tickets, AttractionController attractions,
                              InvoiceController invoices, PassController passes) {
}
