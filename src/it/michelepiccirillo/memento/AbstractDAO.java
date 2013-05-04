package it.michelepiccirillo.memento;

import java.util.List;

public interface AbstractDAO<K, T> {
	public T find(K key);
	
	public List<T> findAll(K... keys);
	
	public long count();
	
	public void deleteAll(K... keys);
	
	public void deleteAll(List<T> objects);
	
	public void delete(T object);
	
	public void insert(T obj);
	
	public List<T> list(int offset, int size);
	
}
