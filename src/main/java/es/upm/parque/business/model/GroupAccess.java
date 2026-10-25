package es.upm.parque.business.model;

/**
 * Access for the members of a group, composed of individual accesses.
 */
public interface GroupAccess extends Access {

    Group getGroup();
}
