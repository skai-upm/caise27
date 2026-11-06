package es.upm.park.business.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Test user directory: each email gets a DNI built from its position (DNI1, DNI2, ...). */
public final class TestUsers {

    private TestUsers() {
    }

    public static UserDirectory of(String... emails) {
        Map<String, String> dnis = new LinkedHashMap<>();
        for (int i = 0; i < emails.length; i++) {
            dnis.put(emails[i], "DNI" + (i + 1));
        }
        return withDnis(dnis);
    }

    public static UserDirectory withDnis(Map<String, String> dnis) {
        return email -> Optional.ofNullable(dnis.get(email));
    }
}
