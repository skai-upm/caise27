package es.upm.parque.presentation;

import es.upm.parque.business.controller.UserController;

/**
 * USER CREATE, USER DELETE and USER UPDATE.
 */
public class UserCommands implements CommandSet {

    private final UserController users;

    public UserCommands(UserController users) {
        this.users = users;
    }

    @Override
    public void registerInto(CommandRegistry registry) {
        registry.register("USER CREATE", "Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> {
                    users.create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
                    return "OK USER CREATED";
                });
        registry.register("USER DELETE", "Correo;Contraseña",
                "Deletes the user if the password matches and it has no ticket dated on or after the system date.",
                p -> {
                    users.delete(p[0], p[1]);
                    return "OK USER DELETED";
                });
        registry.register("USER UPDATE", "Correo;Contraseña;Campo;NuevoValor",
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the current password.",
                p -> {
                    users.update(p[0], p[1], p[2], p[3]);
                    return "OK USER UPDATED";
                });
    }
}
