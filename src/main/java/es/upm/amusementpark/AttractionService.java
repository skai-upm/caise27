package es.upm.amusementpark;

/**
 * Attraction management operations on the park's attractions.
 */
public class AttractionService {

    private final ParkController park = ParkController.getInstance();

    public Attraction createAttraction(String identifier, String name, int capacity)
            throws AttractionOperationException {
        if (identifier.isEmpty() || name.isEmpty()) {
            throw new AttractionOperationException("The attraction identifier and name cannot be empty.");
        }
        if (capacity < 1) {
            throw new AttractionOperationException("The capacity must be a positive whole number.");
        }
        if (park.getAttractions().findById(identifier).isPresent()) {
            throw new AttractionOperationException("An attraction with identifier " + identifier + " already exists.");
        }
        Attraction attraction = new Attraction(identifier, name, capacity);
        park.getAttractions().save(attraction);
        return attraction;
    }

    public Attraction findAttraction(String identifier) throws AttractionOperationException {
        return park.getAttractions().findById(identifier).orElseThrow(
                () -> new AttractionOperationException("No attraction with identifier " + identifier + " exists."));
    }
}
