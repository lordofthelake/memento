package it.michelepiccirillo.memento;

public interface DAOFactory {
	public <K, E, D extends AbstractDAO<K, E>> D getDao(Class<D> daoClass);
	
	public <K, E> AbstractDAO<K, E> getDaoFor(Class<E> entityClass);

}
