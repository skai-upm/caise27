package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.security.BCryptPasswordEncoder;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormat;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import es.upm.amusementpark.presentation.help.SyntaxHelpFormat;

/**
 * Composition root: injects the configured repositories into the park manager and the singleton
 * controllers, establishes their collaborations and builds the command processor.
 */
public final class ApplicationInitializer {

    private ApplicationInitializer() {
    }

    public static CommandProcessor initialize(Repositories r) {
        Park.getInstance().start(r);
        UserService users = UserService.getInstance().inject(r.users(), r.groups(), r.tickets())
                .injectPasswordEncoder(new BCryptPasswordEncoder());
        GroupService groups = GroupService.getInstance().inject(r.users(), r.groups(), r.tickets());
        TicketService tickets = TicketService.getInstance().inject(r.users(), r.tickets(), r.groups())
                .injectPasses(r.passes());
        AttractionService attractions = AttractionService.getInstance().inject(r.attractions(), r.tickets());
        InvoiceService invoices = InvoiceService.getInstance().inject(r.users(), r.groups(), r.tickets())
                .injectPasses(r.passes());
        PassService passes = PassService.getInstance().inject(r.tickets(), r.passes());
        return new CommandProcessor(users, groups, tickets, attractions, invoices, passes,
                new HelpGenerator(new SyntaxHelpFormat()), new HelpGenerator(new ExtendedHelpFormat()));
    }
}
