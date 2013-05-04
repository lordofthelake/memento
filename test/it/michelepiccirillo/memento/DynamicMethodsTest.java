package it.michelepiccirillo.memento;

import static org.junit.Assert.*;

import java.util.List;

import it.michelepiccirillo.memento.DAOFactory;
import it.michelepiccirillo.memento.fixtures.ComplexObject;
import it.michelepiccirillo.memento.jpa.DAOFactoryImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DynamicMethodsTest {
	private static DAOFactory factory;
	private ComplexObject.DAO dao;
	private static EntityManager em;

	private EntityTransaction t;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("memento-junit");
		em = emf.createEntityManager();
		factory = new DAOFactoryImpl(em);
	}
	
	@Before
	public void setUp() {
		dao = factory.getDao(ComplexObject.DAO.class);
		
		t = em.getTransaction();
		t.begin();
		
		em.persist(new ComplexObject(1, "Alpha", 123.45));
		em.persist(new ComplexObject(2, "Alpha", 124.56));
		em.persist(new ComplexObject(3, "Beta", 123.45));
		em.persist(new ComplexObject(4, "Beta", 125.34));
		em.persist(new ComplexObject(5, "Beta", 125.34));
		em.persist(new ComplexObject(6, "Beta", 321.45));
		em.persist(new ComplexObject(7, "Beta", 125.34));
		em.persist(new ComplexObject(8, "Gamma", 123.45));
	}
	
	@Test
	public void testCount() {
		long countByName = dao.countByName("Alpha");
		assertEquals(countByName, 2);
		
		long countByNameAndCost = dao.countByNameAndCost("Beta", 125.34);
		assertEquals(countByNameAndCost, 3);
	}
	
	@Test
	public void testList() {
		List<ComplexObject> byName = dao.listByName(1, 3, "Beta");
		assertNotNull(byName);
		assertEquals(byName.size(), 3);
		assertEquals(byName.get(0).getId(), 4);
		assertEquals(byName.get(1).getId(), 5);
		assertEquals(byName.get(2).getId(), 6);
		
		List<ComplexObject> byNameAndCost = dao.listByNameAndCost(1, 2, "Beta", 125.34);
		assertNotNull(byNameAndCost);
		assertEquals(byNameAndCost.size(), 2);
		assertEquals(byNameAndCost.get(0).getId(), 5);
		assertEquals(byNameAndCost.get(1).getId(), 7);
	}
	
	@Test
	public void testFind() {
		ComplexObject alpha = dao.findByName("Alpha");
		assertNotNull(alpha);
		assertEquals(alpha.getId(), 1);
		
		ComplexObject alpha2 = dao.findByNameAndCost("Alpha", 124.56);
		assertNotNull(alpha2);
		assertEquals(alpha2.getId(), 2);
	}
	
	@Test
	public void testFindAll() {
		List<ComplexObject> byName = dao.findAllByName("Beta");
		assertNotNull(byName);
		assertEquals(byName.size(), 5);
		assertEquals(byName.get(0).getId(), 3);
		assertEquals(byName.get(1).getId(), 4);
		assertEquals(byName.get(2).getId(), 5);
		assertEquals(byName.get(3).getId(), 6);
		assertEquals(byName.get(4).getId(), 7);
		
		List<ComplexObject> byNameAndCost = dao.findAllByNameAndCost("Beta", 125.34);
		assertNotNull(byNameAndCost);
		assertEquals(byNameAndCost.size(), 3);
		assertEquals(byNameAndCost.get(0).getId(), 4);
		assertEquals(byNameAndCost.get(1).getId(), 5);
		assertEquals(byNameAndCost.get(2).getId(), 7);
		
	}
	
	@Test
	public void testDelegation() {
		assertEquals(dao.answerToTheFundamentalQuestion(), 42);
	}

	@Test
	public void testStatement() {
		List<ComplexObject> byStatement = dao.findByCostGreaterThan(200.0);
		assertNotNull(byStatement);
		assertEquals(byStatement.size(), 1);
		assertEquals(byStatement.get(0).getId(), 6);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testUnsupportedMethod() {
		dao.unsupportedMethod();
	}
	
	@After
	public void tearDown() {
		List<ComplexObject> all =  em.createQuery("from ComplexObject", ComplexObject.class).getResultList();
		for(ComplexObject obj : all) {
			em.detach(obj);
		}
		em.createQuery("DELETE from ComplexObject").executeUpdate();
		t.commit();
	}
}
