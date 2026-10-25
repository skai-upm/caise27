package es.upm.parque.business.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.GroupTicketRepository;
import es.upm.parque.persistence.TicketRepository;

/**
 * Business rules for individual and group tickets. All date checks use the shared system date.
 */
public class TicketManager {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");
    public static final String TICKET_CREATED = "OK TICKET CREATED -> ";
    public static final String TICKET_DELETED = "OK TICKET DELETED";
    public static final String PRICE_SET = "OK SYSTEM PRICE -> ";
    public static final String DUPLICATE_TICKET = "ERROR DUPLICATE TICKET";
    public static final String TICKET_NOT_FOUND = "ERROR TICKET NOT FOUND";
    public static final String NOT_REFUNDABLE = "ERROR TICKET NOT REFUNDABLE";
    public static final String PAST_DATE = "ERROR PAST DATE";
    public static final String INVALID_DATE = "ERROR INVALID DATE";
    public static final String INVALID_PRICE = "ERROR INVALID PRICE";
    public static final String GROUP_TICKET_CREATED = "OK GROUP TICKET CREATED -> ";
    public static final String GROUP_TICKET_DELETED = "OK GROUP TICKET DELETED";
    public static final String EMPTY_GROUP = "ERROR EMPTY GROUP";

    private final TicketRepository repository;
    private final GroupTicketRepository groupRepository;
    private final UserManager users;
    private final GroupManager groups;
    private final SystemDate systemDate;
    private final Map<String, IndividualTicket> ticketsById = new LinkedHashMap<>();
    private final Map<String, GroupTicket> groupTicketsById = new LinkedHashMap<>();
    private BigDecimal price = INITIAL_PRICE;

    public TicketManager(TicketRepository repository, GroupTicketRepository groupRepository, UserManager users,
                         GroupManager groups, SystemDate systemDate) throws IOException {
        this.repository = repository;
        this.groupRepository = groupRepository;
        this.users = users;
        this.groups = groups;
        this.systemDate = systemDate;
        for (IndividualTicket ticket : repository.load(users::findByDni)) {
            ticketsById.put(ticket.getId(), ticket);
        }
        for (GroupTicket ticket : groupRepository.load(groups::find, users::findByDni)) {
            groupTicketsById.put(ticket.getId(), ticket);
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String setPrice(String text) {
        BigDecimal value = Ticket.parsePrice(text);
        if (value == null) {
            return INVALID_PRICE;
        }
        price = value;
        return PRICE_SET + price.toPlainString();
    }

    public String create(String id, String email, String dateText) {
        if (!UserValidator.isNotBlank(id)) {
            return UserManager.EMPTY_FIELD;
        }
        if (isIdInUse(id)) {
            return DUPLICATE_TICKET;
        }
        User holder = users.findByEmail(email);
        if (holder == null) {
            return GroupManager.USER_NOT_FOUND;
        }
        String dateError = validateDate(dateText);
        if (dateError != null) {
            return dateError;
        }
        ticketsById.put(id, new IndividualTicket(id, SystemDate.parse(dateText), holder, price));
        return persist(TICKET_CREATED + price.toPlainString());
    }

    public String createGroupTicket(String id, String groupId, String dateText) {
        if (!UserValidator.isNotBlank(id)) {
            return UserManager.EMPTY_FIELD;
        }
        if (isIdInUse(id)) {
            return DUPLICATE_TICKET;
        }
        Group group = groups.find(groupId);
        if (group == null) {
            return GroupManager.GROUP_NOT_FOUND;
        }
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            return EMPTY_GROUP;
        }
        String dateError = validateDate(dateText);
        if (dateError != null) {
            return dateError;
        }
        GroupTicket ticket = new GroupTicket(id, SystemDate.parse(dateText), group, new ArrayList<>(members),
                Collections.nCopies(members.size(), price), GroupTicket.discountFor(members.size()));
        groupTicketsById.put(id, ticket);
        return persist(GROUP_TICKET_CREATED + ticket.getPrice().toPlainString());
    }

    public String deleteGroupTicket(String id) {
        GroupTicket ticket = groupTicketsById.get(id);
        if (ticket == null) {
            return TICKET_NOT_FOUND;
        }
        if (!ticket.isRefundable(systemDate.get())) {
            return NOT_REFUNDABLE;
        }
        groupTicketsById.remove(id);
        return persist(GROUP_TICKET_DELETED);
    }

    /**
     * True while the group has any group ticket dated on or after the system date.
     */
    public boolean isGroupLocked(Group group) {
        LocalDate today = systemDate.get();
        return groupTicketsById.values().stream()
                .anyMatch(t -> t.getGroup() == group && t.isCurrentOrFuture(today));
    }

    public String delete(String id) {
        IndividualTicket ticket = ticketsById.get(id);
        if (ticket == null) {
            return TICKET_NOT_FOUND;
        }
        if (!ticket.isRefundable(systemDate.get())) {
            return NOT_REFUNDABLE;
        }
        ticketsById.remove(id);
        return persist(TICKET_DELETED);
    }

    /**
     * True when the user holds any ticket dated on or after the system date.
     */
    public boolean hasActiveTickets(User user) {
        LocalDate today = systemDate.get();
        return allTickets().stream()
                .anyMatch(t -> t.isCurrentOrFuture(today) && t.getVisitors().stream().anyMatch(v -> v == user));
    }

    /**
     * Finds a standalone individual ticket or a group ticket by identifier.
     */
    public Ticket find(String id) {
        Ticket ticket = ticketsById.get(id);
        return ticket != null ? ticket : groupTicketsById.get(id);
    }

    public IndividualTicket findIndividual(String id) {
        return ticketsById.get(id);
    }

    public GroupTicket findGroupTicket(String id) {
        return groupTicketsById.get(id);
    }

    private List<Ticket> allTickets() {
        List<Ticket> all = new ArrayList<>(ticketsById.values());
        all.addAll(groupTicketsById.values());
        return all;
    }

    private boolean isIdInUse(String id) {
        if (ticketsById.containsKey(id) || groupTicketsById.containsKey(id)) {
            return true;
        }
        return groupTicketsById.values().stream()
                .flatMap(g -> g.getTickets().stream())
                .anyMatch(t -> t.getId().equals(id));
    }

    private String validateDate(String dateText) {
        LocalDate date = SystemDate.parse(dateText);
        if (date == null) {
            return INVALID_DATE;
        }
        return date.isBefore(systemDate.get()) ? PAST_DATE : null;
    }

    private String persist(String success) {
        try {
            repository.save(ticketsById.values());
            groupRepository.save(groupTicketsById.values());
            return success;
        } catch (IOException e) {
            return UserManager.STORAGE_FAILURE;
        }
    }
}
