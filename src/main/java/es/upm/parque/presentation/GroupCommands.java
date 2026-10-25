package es.upm.parque.presentation;

import es.upm.parque.business.controller.GroupController;

/**
 * GROUP CREATE, GROUP ADD, GROUP REMOVE and GROUP DELETE.
 */
public class GroupCommands implements CommandSet {

    private final GroupController groups;

    public GroupCommands(GroupController groups) {
        this.groups = groups;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("GROUP CREATE", "IdGrupo;Nombre", "Creates an empty group.",
                p -> {
                    groups.create(p[0], p[1]);
                    return "OK GROUP CREATED";
                });
        registry.register("GROUP ADD", "IdGrupo;Correo",
                "Adds an existing user to the group (not allowed while the group is locked by a group ticket).",
                p -> {
                    groups.addMember(p[0], p[1]);
                    return "OK GROUP MEMBER ADDED";
                });
        registry.register("GROUP REMOVE", "IdGrupo;Correo",
                "Removes a member from the group (not allowed while the group is locked).",
                p -> {
                    groups.removeMember(p[0], p[1]);
                    return "OK GROUP MEMBER REMOVED";
                });
        registry.register("GROUP DELETE", "IdGrupo", "Deletes the group (not allowed while it is locked).",
                p -> {
                    groups.delete(p[0]);
                    return "OK GROUP DELETED";
                });
    }
}
