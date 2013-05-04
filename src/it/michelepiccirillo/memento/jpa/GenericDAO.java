package it.michelepiccirillo.memento.jpa;

import it.michelepiccirillo.memento.AbstractDAO;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.metamodel.EntityType;

public class GenericDAO<K, T> implements AbstractDAO<K, T> {
	private final Class<T> entityClass;
	private final EntityManager entityManager;
	private final String id;
	
	public GenericDAO(EntityManager em, Class<T> entityClass) {
		this.entityClass = entityClass;
		this.entityManager = em;
		
		EntityType<T> entityType = em.getMetamodel().entity(entityClass);
		this.id = entityType.getId(entityType.getIdType().getJavaType()).getName();
	}
	
	protected EntityManager getEntityManager() {
		return entityManager;
	}

	@Override
	public T find(K key) {
		return entityManager.find(entityClass, key);
	}

	@Override
	public List<T> findAll(K... keys) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		if(keys.length > 0) {
			In<Object> in = cb.in(root.get(id));
			for(K key : keys)
				in.value(key);
			cq.where(in);
		}
		return entityManager.createQuery(cq).getResultList();
		
	}

	@Override
	public long count() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(entityClass)));
		return entityManager.createQuery(cq).getSingleResult();
	}

	@Override
	public void insert(T obj) {
		entityManager.persist(obj);
	}

	@Override
	public List<T> list(int offset, int size) {
		CriteriaQuery<T> cq = entityManager.getCriteriaBuilder().createQuery(entityClass);
		cq.from(entityClass);
		return entityManager.createQuery(cq)
				.setFirstResult(offset)
				.setMaxResults(size)
				.getResultList();
	}

	@Override
	public void deleteAll(List<T> objects) {
		for(T obj : objects) entityManager.remove(obj);
	}

	@Override
	public void deleteAll(K... keys) {
		List<K> ids = Arrays.asList(keys);
		TypedQuery<T> select = entityManager.createQuery("SELECT o from " + entityClass.getName() + " o WHERE " + id +" IN (:in)", entityClass);
		select.setParameter("in", ids);
		for(T obj : select.getResultList())
			entityManager.detach(obj);
		Query delete = entityManager.createQuery("DELETE from " + entityClass.getName() + " WHERE " + id + " IN (:in)");
		delete.setParameter("in", ids).executeUpdate();
		entityManager.flush();
	}

	@Override
	public void delete(T obj) {
		entityManager.remove(obj);
	}
}
