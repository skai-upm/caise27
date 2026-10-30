package es.upm.etsisi.amusementpark.presentation.command;

import es.upm.etsisi.amusementpark.business.controller.GroupController;

/**
 * GROUP CREATE / ADD / REMOVE / DELETE.
 */
public class GroupCommands implements CommandModule {

    private final GroupController groups;

    public GroupCommands(GroupController groups) {
        this.groups = groups;
    }

    @Override
    public void registerIn(CommandRegistry registry) {
        registry.register("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group with a unique identifier.",
                p -> {
                    groups.create(p[1], p[2]);
                    return "GROUP CREATED";
                });
        registry.register("GROUP ADD;<Identifier>;<Email>",
                "Adds an existing user to a group not locked by an active group ticket.",
                p -> {
                    groups.addUser(p[1], p[2]);
                    return "GROUP USER ADDED";
                });
        registry.register("GROUP REMOVE;<Identifier>;<Email>",
                "Removes a member from a group not locked by an active group ticket.",
                p -> {
                    groups.removeUser(p[1], p[2]);
                    return "GROUP USER REMOVED";
                });
        registry.register("GROUP DELETE;<Identifier>",
                "Deletes a group without active group tickets; historical group tickets are kept.",
                p -> {
                    groups.delete(p[1]);
                    return "GROUP DELETED";
                });
    }
}
