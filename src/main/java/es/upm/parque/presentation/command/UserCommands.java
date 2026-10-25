package es.upm.parque.presentation.command;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.UserField;
import es.upm.parque.presentation.CommandRegistry;

/**
 * USER commands.
 */
public final class UserCommands {

    private UserCommands() {
    }

    public static void register(CommandRegistry registry, UserController users) {
        registry.register("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "Registers a user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> {
                    users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    return "OK USER CREATED";
                });
        registry.register("USER DELETE;Correo;Contraseña",
                "Deletes the user if the password is correct and they have no ticket dated today or later.",
                p -> {
                    users.delete(p[1], p[2]);
                    return "OK USER DELETED";
                });
        registry.register("USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD after checking the password; the DNI cannot change.",
                p -> {
                    UserField field = UserField.fromName(p[3]);
                    if (field == null) {
                        throw new ParkException("field '" + p[3]
                                + "' cannot be updated. Valid fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD");
                    }
                    users.update(p[1], p[2], field, p[4]);
                    return "OK USER UPDATED";
                });
    }
}
