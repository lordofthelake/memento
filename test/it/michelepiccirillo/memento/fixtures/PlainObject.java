package it.michelepiccirillo.memento.fixtures;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PlainObject {
	/**
	 * @author   m
	 */
	public static enum SampleEnumeration { /**
	 * @uml.property  name="cHOICE_1"
	 * @uml.associationEnd  
	 */
	CHOICE_1, /**
	 * @uml.property  name="cHOICE_2"
	 * @uml.associationEnd  
	 */
	CHOICE_2, /**
	 * @uml.property  name="cHOICE_3"
	 * @uml.associationEnd  
	 */
	CHOICE_3 };
	
	/**
	 * @uml.property  name="id"
	 */
	@Id
	private int id;
	
	/**
	 * @uml.property  name="title"
	 */
	private String title;
	
	public PlainObject() {}
	
	public PlainObject(int id, String title) {
		this.id = id;
		this.title = title;
	}
	
	/**
	 * @return
	 * @uml.property  name="id"
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return
	 * @uml.property  name="title"
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 * @uml.property  name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @param i
	 * @uml.property  name="id"
	 */
	public void setId(int i) {
		this.id = i;
	}
	
}
