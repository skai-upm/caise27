package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.etsisi.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.etsisi.amusementpark.business.model.Admission;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.SpecialAttraction;
import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;
import es.upm.etsisi.amusementpark.persistence.TextRepositories;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Delivery 3 technical restrictions gathered in one place.
 */
class Delivery3RestrictionsTest {

    @Test
    void builderForInvoices() {
        assertTrue(Modifier.isAbstract(InvoiceBuilder.class.getModifiers()));
        assertEquals(InvoiceBuilder.class, UserInvoiceBuilder.class.getSuperclass());
        assertEquals(InvoiceBuilder.class, GroupInvoiceBuilder.class.getSuperclass());
    }

    @Test
    void singletonControllers() throws Exception {
        for (Class<?> type : List.of(ParkController.class, InvoiceController.class)) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), type.getSimpleName());
            }
            Field instance = type.getDeclaredField("INSTANCE");
            assertTrue(Modifier.isStatic(instance.getModifiers()) && Modifier.isFinal(instance.getModifiers()));
        }
    }

    @Test
    void repositoriesAreConfigurableInstances() {
        ParkRepositories first = TextRepositories.inDirectory(Path.of("data"));
        ParkRepositories second = TextRepositories.inDirectory(Path.of("tmp/x"));
        assertNotSame(first.users(), second.users());
        assertNotSame(first.passes(), second.passes());
    }

    @Test
    void bcryptIsAMavenDependency() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        assertTrue(pom.contains("<groupId>org.mindrot</groupId>") && pom.contains("<artifactId>jbcrypt</artifactId>"));
        assertTrue(pom.contains("<maven.compiler.release>21</maven.compiler.release>"));
    }

    @Test
    void attractionsUseBoundedGenerics() {
        TypeVariable<?>[] parameters = Attraction.class.getTypeParameters();
        assertEquals(1, parameters.length);
        assertEquals(Admission.class, parameters[0].getBounds()[0]);
        assertTrue(Attraction.class.isAssignableFrom(SpecialAttraction.class));
    }

    @Test
    void layeredPackagesExist() {
        Path root = Path.of("src/main/java/es/upm/etsisi/amusementpark");
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertTrue(Files.isDirectory(root.resolve(layer)), layer);
        }
    }
}
