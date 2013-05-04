package it.michelepiccirillo.memento;

import static org.junit.Assert.*;
import it.michelepiccirillo.memento.fixtures.PlainObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

/**
 * Semplice test per verificare le impostazioni del persistence.xml e il funzionamento delle librerie.
 */
public class PersistenceTest {

	@Test
	public void test() {
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("memento-junit");
			EntityManager em = emf.createEntityManager();
			
			PlainObject entity = new PlainObject();
			entity.setTitle("Sample string");
			entity.setId(123);
			
			em.persist(entity);
			
			PlainObject retrieved = em.find(PlainObject.class, 123);
			assertNotNull(retrieved);
			assertEquals(entity.getTitle(), "Sample string");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Wrong DB settings or missing libraries.");
		}
		
	}

}
