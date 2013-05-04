package it.michelepiccirillo.memento;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import it.michelepiccirillo.memento.AbstractDAO;
import it.michelepiccirillo.memento.DAOFactory;
import it.michelepiccirillo.memento.fixtures.PlainObject;
import it.michelepiccirillo.memento.jpa.DAOFactoryImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GenericDAOTest {
	private static DAOFactory factory;
	private AbstractDAO<Integer, PlainObject> dao;
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
		dao = factory.getDaoFor(PlainObject.class);
		
		t = em.getTransaction();
		t.begin();
		
		em.persist(new PlainObject(1, "Object 1"));
		em.persist(new PlainObject(2, "Object 2"));
		em.persist(new PlainObject(3, "Object 3"));
		em.persist(new PlainObject(4, "Object 4"));
		em.persist(new PlainObject(5, "Object 5"));
		em.persist(new PlainObject(6, "Object 6"));
	}
	
	@Test
	public void testFind() {
		PlainObject obj = dao.find(3);
		assertNotNull(obj);
		assertEquals(obj.getId(), 3);
		assertEquals(obj.getTitle(), "Object 3");
	}

	@Test
	public void testFindAll() {
		List<PlainObject> list = dao.findAll();
		assertNotNull(list);
		assertEquals(list.size(), 6);
		for(int i = 0; i < list.size(); ++i) {
			PlainObject obj = list.get(i);
			assertEquals(obj.getId(), i+1);
			assertEquals(obj.getTitle(), "Object " + (i+1));
		}
		
		List<PlainObject> byId = dao.findAll(3, 4, 5);
		assertNotNull(byId);
		assertEquals(byId.size(), 3);
		assertEquals(byId.get(0).getId(), 3);
		assertEquals(byId.get(1).getId(), 4);
		assertEquals(byId.get(2).getId(), 5);
	}

	@Test
	public void testCount() {
		assertEquals(dao.count(), 6);
	}

	@Test
	public void testDeleteKeys() {
		dao.deleteAll(4, 5);
		assertNull(em.find(PlainObject.class, 4));
		assertNull(em.find(PlainObject.class, 5));
	}

	@Test
	public void testInsert() {
		dao.insert(new PlainObject(7, "Object 7"));
		
		PlainObject retrieved = em.find(PlainObject.class, 7);
		assertNotNull(retrieved);
		assertEquals(retrieved.getId(), 7);
		assertEquals(retrieved.getTitle(), "Object 7");
	}

	@Test
	public void testList() {
		List<PlainObject> list = dao.list(2, 3);
		assertEquals(list.size(), 3);
		assertEquals(list.get(0).getId(), 3);
		assertEquals(list.get(2).getId(), 5);
	}

	@Test
	public void testDeleteObjects() {
		List<PlainObject> list = new LinkedList<PlainObject>();
		list.add(em.find(PlainObject.class, 4));
		list.add(em.find(PlainObject.class, 5));
		
		dao.deleteAll(list);
		
		assertNull(em.find(PlainObject.class, 4));
		assertNull(em.find(PlainObject.class, 5));
		
	}
	
	@After
	public void tearDown() {
		List<PlainObject> all =  em.createQuery("from PlainObject", PlainObject.class).getResultList();
		for(PlainObject obj : all) {
			em.detach(obj);
		}
		em.createQuery("DELETE from PlainObject").executeUpdate();
		t.commit();
	}

}
