package es.upm.parque.presentation.command;

import es.upm.parque.business.controller.GroupController;
import es.upm.parque.presentation.CommandRegistry;

/**
 * GROUP commands.
 */
public final class GroupCommands {

    private GroupCommands() {
    }

    public static void register(CommandRegistry registry, GroupController groups) {
        registry.register("GROUP CREATE;IdGrupo;Nombre", "Creates an empty group.",
                p -> {
                    groups.create(p[1], p[2]);
                    return "OK GROUP CREATED";
                });
        registry.register("GROUP ADD;IdGrupo;Correo",
                "Adds a registered user to the group (not while it has a group ticket dated today or later).",
                p -> {
                    groups.addMember(p[1], p[2]);
                    return "OK GROUP USER ADDED";
                });
        registry.register("GROUP REMOVE;IdGrupo;Correo",
                "Removes a member from the group (same lock as GROUP ADD).",
                p -> {
                    groups.removeMember(p[1], p[2]);
                    return "OK GROUP USER REMOVED";
                });
        registry.register("GROUP DELETE;IdGrupo",
                "Deletes the group, keeping its users (same lock as GROUP ADD).",
                p -> {
                    groups.delete(p[1]);
                    return "OK GROUP DELETED";
                });
    }
}
