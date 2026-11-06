package es.upm.park.business.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory pass port for model tests: passes per ticket id, consumption recorded. */
public class TestEntryRules implements EntryRules {

    private final Map<String, List<String>> speed = new LinkedHashMap<>();
    private final Map<String, String> special = new LinkedHashMap<>();
    final List<String> consumed = new ArrayList<>();

    public TestEntryRules speedPass(String ticketId, String passId) {
        speed.computeIfAbsent(ticketId, k -> new ArrayList<>()).add(passId);
        return this;
    }

    public TestEntryRules specialPass(String ticketId, String passId) {
        special.put(ticketId, passId);
        return this;
    }

    @Override
    public Optional<String> availableSpeedPass(IndividualAccess ticket) {
        return speed.getOrDefault(ticket.getId(), List.of()).stream().filter(id -> !consumed.contains(id)).findFirst();
    }

    @Override
    public Optional<String> availableSpecialPass(IndividualAccess ticket) {
        return Optional.ofNullable(special.get(ticket.getId())).filter(id -> !consumed.contains(id));
    }

    @Override
    public void consume(String passId) {
        consumed.add(passId);
    }
}
