package com.amusementpark.business.model;

/**
 * Admits group tickets only.
 */
public class GroupAttraction extends Attraction<GroupAdmission> {

    public GroupAttraction(String id, String name, int capacity) {
        super(id, name, capacity, GroupAdmission.class);
    }

    @Override
    public AttractionType getType() {
        return AttractionType.GROUP;
    }
}
