package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.util.List;

/** Decides which construction steps apply to each kind of invoice. */
public class InvoiceDirector {

    public Invoice userInvoice(User user, List<IndividualTicket> directTickets) {
        return new UserInvoiceBuilder().addUser(user).addIndividualTickets(directTickets).build();
    }

    public Invoice groupInvoice(Group group, List<GroupTicket> groupTickets) {
        return new GroupInvoiceBuilder().addGroup(group).addGroupTickets(groupTickets).build();
    }
}
