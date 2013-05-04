package it.michelepiccirillo.memento;

import static org.junit.Assert.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import it.michelepiccirillo.memento.AbstractDAO;
import it.michelepiccirillo.memento.DAOFactory;
import it.michelepiccirillo.memento.fixtures.ExternalDAO;
import it.michelepiccirillo.memento.fixtures.ObjectWithDeclaredDAO;
import it.michelepiccirillo.memento.fixtures.ObjectWithImplicitDAO;
import it.michelepiccirillo.memento.fixtures.PlainObject;
import it.michelepiccirillo.memento.jpa.DAOFactoryImpl;

import org.junit.BeforeClass;
import org.junit.Test;

public class DAODetectionTest {
	private static DAOFactory factory;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("memento-junit");
		factory = new DAOFactoryImpl(emf.createEntityManager());
	}
	
	@Test
	public void testObjectWithoutDAO() {
		AbstractDAO<Integer, PlainObject> dao = factory.getDaoFor(PlainObject.class);
		assertNotNull(dao);
	}
	

	@Test
	public void testObjectWithImplicitDAO() {
		AbstractDAO<?, ObjectWithImplicitDAO> dao = factory.getDaoFor(ObjectWithImplicitDAO.class);
		assertNotNull(dao);
		assertTrue(dao instanceof ObjectWithImplicitDAO.DAO);
		
		AbstractDAO<?, ObjectWithImplicitDAO> genericDao = factory.getDaoFor(ObjectWithImplicitDAO.class);
		assertNotNull(genericDao);
		assertTrue(dao instanceof ObjectWithImplicitDAO.DAO);
	}

	@Test
	public void testObjectWithDeclaredDAO() {
		AbstractDAO<Integer, ObjectWithDeclaredDAO> dao = factory.getDaoFor(ObjectWithDeclaredDAO.class);
		assertNotNull(dao);
		assertTrue(dao instanceof ExternalDAO);
		
		AbstractDAO<Integer, ObjectWithDeclaredDAO> genericDao = factory.getDaoFor(ObjectWithDeclaredDAO.class);
		assertNotNull(genericDao);
		assertTrue(dao instanceof ExternalDAO);
	}

}
