package es.upm.etsisi.park.presentation.command;

import es.upm.etsisi.park.business.controller.GroupController;
import es.upm.etsisi.park.presentation.Args;
import es.upm.etsisi.park.presentation.CommandDefinition;
import es.upm.etsisi.park.presentation.CommandGroup;
import es.upm.etsisi.park.presentation.CommandRegistry;

/** GROUP CREATE, GROUP ADD, GROUP REMOVE and GROUP DELETE. */
public class GroupCommands implements CommandGroup {

    static final String CREATE = "GROUP CREATE;<identifier>;<name>";
    static final String ADD = "GROUP ADD;<identifier>;<email>";
    static final String REMOVE = "GROUP REMOVE;<identifier>;<email>";
    static final String DELETE = "GROUP DELETE;<identifier>";

    private final GroupController groups;

    public GroupCommands(GroupController groups) {
        this.groups = groups;
    }

    @Override
    public void register(CommandRegistry registry) {
        registry.register(CommandDefinition.of("GROUP CREATE", "Creates an empty group of users.",
                (args, out) -> {
                    Args.require(args, CREATE, 2);
                    groups.create(args[0], args[1]);
                    out.println("GROUP CREATED");
                }, CREATE));
        registry.register(CommandDefinition.of("GROUP ADD",
                "Adds an existing user to a group, unless an active group ticket locks it.",
                (args, out) -> {
                    Args.require(args, ADD, 2);
                    groups.addMember(args[0], args[1]);
                    out.println("USER ADDED TO GROUP");
                }, ADD));
        registry.register(CommandDefinition.of("GROUP REMOVE",
                "Removes a member from a group, unless an active group ticket locks it.",
                (args, out) -> {
                    Args.require(args, REMOVE, 2);
                    groups.removeMember(args[0], args[1]);
                    out.println("USER REMOVED FROM GROUP");
                }, REMOVE));
        registry.register(CommandDefinition.of("GROUP DELETE",
                "Deletes a group without active group tickets; historical group tickets are kept.",
                (args, out) -> {
                    Args.require(args, DELETE, 1);
                    groups.delete(args[0]);
                    out.println("GROUP DELETED");
                }, DELETE));
    }
}
