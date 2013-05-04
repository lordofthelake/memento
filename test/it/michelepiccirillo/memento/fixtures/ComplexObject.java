package it.michelepiccirillo.memento.fixtures;

import java.util.List;

import javax.persistence.*;

import it.michelepiccirillo.memento.AbstractDAO;
import it.michelepiccirillo.memento.Delegate;
import it.michelepiccirillo.memento.jpa.Statement;

@Entity
public class ComplexObject {
	public static interface DAO extends AbstractDAO<Integer, ComplexObject> {
		public List<ComplexObject> listByName(int off, int count, String name);
		public List<ComplexObject> listByNameAndCost(int off, int count, String name, double cost);
		
		public List<ComplexObject> findAllByName(String name);
		public List<ComplexObject> findAllByNameAndCost(String name, double cost);
		
		public ComplexObject findByName(String name);
		public ComplexObject findByNameAndCost(String name, double cost);
		
		long countByName(String name);
		long countByNameAndCost(String name, double cost);
		
		void unsupportedMethod();
		
		@Statement("SELECT obj FROM ComplexObject obj WHERE cost > ?")
		List<ComplexObject> findByCostGreaterThan(double cost);
		
		@Delegate(ComplexObject.class)
		int answerToTheFundamentalQuestion();
	}
	
	public static int answerToTheFundamentalQuestion() {
		return 42;
	}
	
	@Id
	private int id;
	private String name;
	private double cost;
	
	@SuppressWarnings("unused")
	private ComplexObject() {}
	
	public ComplexObject(int id, String name, double cost) {
		this.id = id;
		this.name = name;
		this.cost = cost;
	}

	/**
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	
}
