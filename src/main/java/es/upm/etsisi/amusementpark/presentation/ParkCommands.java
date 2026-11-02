package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.group.GroupController;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.park.Prices;
import es.upm.etsisi.amusementpark.business.pass.PassController;
import es.upm.etsisi.amusementpark.business.pass.Pass;
import es.upm.etsisi.amusementpark.business.pass.SpecialPass;
import es.upm.etsisi.amusementpark.business.pass.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.TicketController;
import java.math.BigDecimal;
import java.util.List;

/**
 * SYSTEM, GROUP, TICKET, GROUPTICKET, SPEEDPASS and SPECIALPASS commands.
 */
public final class ParkCommands {

    private ParkCommands() {
    }

    public static void register(CommandRegistry registry, Park park, GroupController groups,
                                TicketController tickets, PassController passes) {
        registerSystem(registry, park);
        registerGroups(registry, groups);
        registerTickets(registry, tickets);
        registerPasses(registry, passes);
    }

    private static void registerSystem(CommandRegistry registry, Park park) {
        registry.register(CommandSpec.of("SYSTEM DATE;<yyyy-MM-dd>",
                "Sets the current system date used by every date-dependent operation.", p -> {
                    park.setCurrentDate(Args.date(p[1]));
                    return CommandResult.message("SYSTEM DATE SET TO " + park.getCurrentDate());
                }));
        registry.register(CommandSpec.of("SYSTEM PRICE;<Price>",
                "Sets the base price of tickets created from now on in this execution.", p -> {
                    BigDecimal price = Args.price(p[1]);
                    park.setBasePrice(price);
                    return CommandResult.message("SYSTEM PRICE SET TO " + Prices.format(price));
                }));
    }

    private static void registerGroups(CommandRegistry registry, GroupController groups) {
        registry.register(CommandSpec.of("GROUP CREATE;<Identifier>;<Name>",
                "Creates an empty group with a unique identifier.", p -> {
                    groups.create(p[1], p[2]);
                    return CommandResult.message("GROUP CREATED");
                }));
        registry.register(CommandSpec.of("GROUP ADD;<Identifier>;<Email>",
                "Adds an existing user to a group without active group tickets.", p -> {
                    groups.addMember(p[1], p[2]);
                    return CommandResult.message("USER ADDED TO GROUP");
                }));
        registry.register(CommandSpec.of("GROUP REMOVE;<Identifier>;<Email>",
                "Removes a member from a group without active group tickets.", p -> {
                    groups.removeMember(p[1], p[2]);
                    return CommandResult.message("USER REMOVED FROM GROUP");
                }));
        registry.register(CommandSpec.of("GROUP DELETE;<Identifier>",
                "Deletes a group without active group tickets; its historical tickets are kept.", p -> {
                    groups.delete(p[1]);
                    return CommandResult.message("GROUP DELETED");
                }));
    }

    private static void registerTickets(CommandRegistry registry, TicketController tickets) {
        registry.register(CommandSpec.of("TICKET CREATE;<identifier>;<email>;<date>",
                "Buys an individual ticket for a user and a date not in the past at the current base price.", p -> {
                    IndividualTicket t = tickets.create(p[1], p[2], Args.date(p[3]));
                    return CommandResult.message("TICKET CREATED; PRICE: " + Prices.format(t.getPrice()));
                }));
        registry.register(CommandSpec.of("TICKET DELETE;<identifier>",
                "Refunds a future standalone ticket together with all its passes.", p -> {
                    tickets.delete(p[1]);
                    return CommandResult.message("TICKET DELETED");
                }));
        registry.register(CommandSpec.of("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "Buys a discounted ticket for every current member of a non-empty group.", p -> {
                    GroupTicket t = tickets.createGroupTicket(p[1], p[2], Args.date(p[3]));
                    return CommandResult.message("GROUP TICKET CREATED; MEMBERS: " + t.composition().size()
                            + "; TOTAL PRICE: " + Prices.format(t.getPrice()));
                }));
        registry.register(CommandSpec.of("GROUPTICKET DELETE;<identifier>",
                "Refunds a future group ticket, its member tickets and all their passes.", p -> {
                    tickets.deleteGroupTicket(p[1]);
                    return CommandResult.message("GROUP TICKET DELETED");
                }));
    }

    private static void registerPasses(CommandRegistry registry, PassController passes) {
        registry.register(CommandSpec.of("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "Buys 1 or 10 SpeedPasses for an individual ticket, each costing 20% of the ticket price.", p -> {
                    List<SpeedPass> bought = passes.buySpeedPasses(p[1], p[2], Args.integer(p[3], "quantity"));
                    BigDecimal total = bought.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return CommandResult.message("SPEEDPASS CREATED: " + bought.get(0).getId()
                            + (bought.size() > 1 ? " .. " + bought.get(bought.size() - 1).getId() : "")
                            + "; TOTAL PRICE: " + Prices.format(total));
                }));
        registry.register(CommandSpec.of("SPEEDPASS DELETE;<speedPassIdentifier>",
                "Refunds an unused SpeedPass whose ticket is dated after today.", p -> {
                    passes.refundSpeedPass(p[1]);
                    return CommandResult.message("SPEEDPASS DELETED");
                }));
        registry.register(CommandSpec.of("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "Buys the single Special Pass of an individual ticket, costing the ticket price.", p -> {
                    SpecialPass bought = passes.buySpecialPass(p[1], p[2]);
                    return CommandResult.message("SPECIALPASS CREATED; PRICE: " + Prices.format(bought.getPrice()));
                }));
        registry.register(CommandSpec.of("SPECIALPASS DELETE;<specialPassIdentifier>",
                "Refunds an unused Special Pass whose ticket is dated after today.", p -> {
                    passes.refundSpecialPass(p[1]);
                    return CommandResult.message("SPECIALPASS DELETED");
                }));
    }
}
