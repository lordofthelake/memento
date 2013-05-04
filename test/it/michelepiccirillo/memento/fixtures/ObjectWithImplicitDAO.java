package it.michelepiccirillo.memento.fixtures;

import javax.persistence.Entity;
import javax.persistence.Id;

import it.michelepiccirillo.memento.AbstractDAO;

@Entity
public class ObjectWithImplicitDAO {
	public static interface DAO extends AbstractDAO<Integer, ObjectWithImplicitDAO> {}
	
	@Id
	private int id;
}
