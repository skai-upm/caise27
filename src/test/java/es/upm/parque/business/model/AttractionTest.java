package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.parque.Fixtures;

class AttractionTest {

    @Test
    void tracksPresentUsersAndCapacity() {
        Attraction tornado = new Attraction("A01", "Tornado", 3);
        List<User> group = Fixtures.users(2);
        User extra = Fixtures.ana();
        assertEquals("A01", tornado.getId());
        assertEquals("Tornado", tornado.getName());
        assertEquals(3, tornado.getCapacity());
        assertTrue(tornado.isEmpty());
        assertTrue(tornado.canAdmit(group));
        tornado.admit(group);
        assertEquals(1, tornado.available());
        assertTrue(tornado.contains(group.get(0)));
        assertFalse(tornado.canAdmit(Fixtures.users(2)));
        assertTrue(tornado.canAdmit(List.of(extra)));
        assertEquals(0, tornado.release(List.of(extra)));
        assertEquals(2, tornado.release(group));
        assertTrue(tornado.isEmpty());
        List<User> present = tornado.getPresent();
        assertThrows(UnsupportedOperationException.class, () -> present.add(extra));
    }
}
