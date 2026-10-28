package es.upm.amusementpark.business.service;

import java.util.List;

/**
 * Snapshot of the current state of an attraction.
 */
public record AttractionStatus(String id, String name, int capacity, int occupants, int available,
        List<String> occupantEmails) {
}
