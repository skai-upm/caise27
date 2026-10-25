package es.upm.parque.business.model;

/**
 * Access capability of a whole group, made of the individual accesses of its members.
 */
public interface GroupAccess extends AccessCapability {

    Group getGroup();
}
