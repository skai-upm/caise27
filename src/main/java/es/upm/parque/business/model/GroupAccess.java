package es.upm.parque.business.model;

/**
 * Access capability of a credential that admits the members of a group.
 */
public interface GroupAccess extends AccessCredential {

    Group getGroup();
}
