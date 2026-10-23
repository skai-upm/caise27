package es.upm.etsisi.park.presentation.command;

import java.math.BigDecimal;
import java.util.List;

import es.upm.etsisi.park.business.controller.PassController;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.business.model.ticket.Money;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** SPEEDPASS CREATE/DELETE and SPECIALPASS CREATE/DELETE. */
public class PassCommands implements CommandGroup {

    static final String SPEED_CREATE = "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>";
    static final String SPEED_DELETE = "SPEEDPASS DELETE;<speedPassIdentifier>";
    static final String SPECIAL_CREATE = "SPECIALPASS CREATE;<identifier>;<individualTicket>";
    static final String SPECIAL_DELETE = "SPECIALPASS DELETE;<specialPassIdentifier>";

    private final PassController passes;

    public PassCommands(PassController passes) {
        this.passes = passes;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("SPEEDPASS CREATE",
                "Buys 1 or 10 SpeedPasses (ids <purchaseIdentifier>:<n>) for an individual ticket; each costs 20% of the ticket.",
                (args, out) -> {
                    Args.require(args, SPEED_CREATE, 3);
                    List<SpeedPass> created = passes.createSpeedPasses(args[0], args[1], args[2]);
                    BigDecimal unit = created.get(0).getPrice();
                    BigDecimal total = unit.multiply(BigDecimal.valueOf(created.size()));
                    String ids = created.size() == 1 ? created.get(0).getId()
                            : created.get(0).getId() + " to " + created.get(created.size() - 1).getId();
                    out.println("SPEEDPASS CREATED: " + ids + " for " + created.get(0).getTicket().getId() + ", "
                            + created.size() + " x " + Money.format(unit) + ", total " + Money.format(total));
                }, SPEED_CREATE).withNotes("<quantity> is 1 or 10"));
        registry.register(CommandDefinition.of("SPEEDPASS DELETE",
                "Refunds one unconsumed SpeedPass of a ticket dated after today.",
                (args, out) -> {
                    Args.require(args, SPEED_DELETE, 1);
                    passes.deleteSpeedPass(args[0]);
                    out.println("SPEEDPASS DELETED");
                }, SPEED_DELETE));
        registry.register(CommandDefinition.of("SPECIALPASS CREATE",
                "Buys the Special Pass of an individual ticket (at most one), needed for SPECIAL attractions; costs as the ticket.",
                (args, out) -> {
                    Args.require(args, SPECIAL_CREATE, 2);
                    SpecialPass pass = passes.createSpecialPass(args[0], args[1]);
                    out.println("SPECIALPASS CREATED: " + pass.getId() + " for " + pass.getTicket().getId()
                            + ", price " + Money.format(pass.getPrice()));
                }, SPECIAL_CREATE));
        registry.register(CommandDefinition.of("SPECIALPASS DELETE",
                "Refunds an unconsumed Special Pass of a ticket dated after today.",
                (args, out) -> {
                    Args.require(args, SPECIAL_DELETE, 1);
                    passes.deleteSpecialPass(args[0]);
                    out.println("SPECIALPASS DELETED");
                }, SPECIAL_DELETE));
    }
}
