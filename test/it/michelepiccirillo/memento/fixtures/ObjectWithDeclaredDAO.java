package it.michelepiccirillo.memento.fixtures;

import it.michelepiccirillo.memento.PersistedBy;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@PersistedBy(ExternalDAO.class)
public class ObjectWithDeclaredDAO {
	/**
	 * @uml.property  name="id"
	 */
	@Id
	private int id;
}
