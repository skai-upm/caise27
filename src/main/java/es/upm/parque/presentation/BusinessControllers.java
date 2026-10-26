package es.upm.parque.presentation;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;

/**
 * The business controllers the presentation layer works with, injected as a whole at startup.
 */
public record BusinessControllers(UserController users, GroupController groups, TicketController tickets,
                                  AttractionController attractions, InvoiceController invoices,
                                  PassController passes) {
}
