package es.upm.amusementpark.service;

import java.util.List;
import java.util.Optional;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.AttractionRepository;

/**
 * Attraction management.
 */
public class AttractionService {

    private final AttractionRepository repository;

    public AttractionService(AttractionRepository repository) {
        this.repository = repository;
    }

    public Optional<Attraction> findById(String id) {
        return repository.findById(id);
    }

    public List<Attraction> findAll() {
        return repository.findAll();
    }

    public Attraction createAttraction(String id, String name, int capacity) throws OperationException {
        if (id.isEmpty() || name.isEmpty()) {
            throw new OperationException("Attraction identifier and name are required");
        }
        if (capacity < 1) {
            throw new OperationException("Capacity must be at least 1");
        }
        if (repository.findById(id).isPresent()) {
            throw new OperationException("An attraction with this identifier already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        repository.save(attraction);
        return attraction;
    }

    /**
     * Emails of the users currently inside the attraction. Read-only.
     */
    public List<String> listOccupantEmails(String id) throws OperationException {
        return existing(id).getOccupants().stream().map(User::getEmail).toList();
    }

    Attraction existing(String id) throws OperationException {
        return repository.findById(id).orElseThrow(() -> new OperationException("Attraction not found"));
    }
}
