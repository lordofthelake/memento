package it.michelepiccirillo.memento.jpa;

import it.michelepiccirillo.memento.AbstractDAO;
import it.michelepiccirillo.memento.DAOFactory;
import it.michelepiccirillo.memento.Delegate;
import it.michelepiccirillo.memento.PersistedBy;
import it.michelepiccirillo.memento.Persists;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;


public class DAOFactoryImpl implements DAOFactory {
	private class ProxyInvocationHandler<K, E> implements InvocationHandler {
		
		GenericDAO<K, E> baseDao;
		Class<E> entityClass;
				
		@SuppressWarnings("unchecked")
		ProxyInvocationHandler(Class<E> entityClass) {
			baseDao = (GenericDAO<K, E>) createGenericImpl(entityManager, entityClass);
		}
		
		@Override
		public Object invoke(Object obj, Method method, Object[] args)
				throws Throwable {
			String methodName = method.getName();
			
			Delegate delegate = method.getAnnotation(Delegate.class);
			if(delegate != null) {
				try {
					Class<?> delegateTo = delegate.value();
					String delegateMethod = delegate.method();
					
					if(delegateMethod.isEmpty()) 
						delegateMethod = method.getName();
					
					return delegateTo
							.getMethod(methodName, method.getParameterTypes())
							.invoke(delegateTo, args);
				} catch(InvocationTargetException itEx) {
					throw itEx.getCause();
				} catch (Exception ex) {
					throw ex;
				}
			}
			
			int argsOffset = 0, resultsOffset = 0, resultsLimit = -1;
			
			Class<?> returnType = method.getReturnType();		
			boolean noReturn = returnType.equals(Void.TYPE);
			boolean multiple = returnType.equals(List.class);
			
			Statement statement = method.getAnnotation(Statement.class);
			String jpql = null;
			
			if(statement != null) { // method is annotated with the custom query to execute
				jpql = statement.value();
			} else if(method.getName().contains("By")) { // dynamic method
				int byPosition = methodName.indexOf("By");
				
				String verb = methodName.substring(0, byPosition);
				String[] filters = methodName.substring(byPosition + 2).split("And");
				
				if("list".equals(verb)) {
					argsOffset = 2;
					resultsOffset = (Integer) args[0];
					resultsLimit = (Integer) args[1];
					verb = "findAll";
				}
				
				if("find".equals(verb)) {
					resultsLimit = 1;
				}
				
				if(filters.length != (args.length - argsOffset))
					throw new IllegalArgumentException("Arguments mismatch for method " 
							+ method + ": " + filters.length + " filters for " + (args.length - argsOffset) + " arguments");
				
				StringBuffer jpqlBuf = null;
				
				if("findAll".equals(verb) || "find".equals(verb)) {
					jpqlBuf = new StringBuffer("SELECT obj ");
				} else if("count".equals(verb)) {
					jpqlBuf = new StringBuffer("SELECT COUNT(*) ");
				} else {
					throw new UnsupportedOperationException("'" + verb + "' is not a valid verb for auto-generation of " + method);
				}
				
				jpqlBuf.append("FROM ").append(entityClass.getName()).append(" obj");
				
				if(filters.length > 0) {
					jpqlBuf.append(" WHERE ");
					for(int i = 0; i < filters.length; ++i) {
						if(i != 0)
							jpqlBuf.append(" AND ");
						jpqlBuf.append(Character.toLowerCase(filters[i].charAt(0)));
						jpqlBuf.append(filters[i].substring(1));
						jpqlBuf.append(" = ?");
					}
				}
				
				jpql = jpqlBuf.toString();
			}
			
			if(jpql != null) {
				Query query = entityManager.createQuery(jpql);
				for(int i = argsOffset; i < args.length; ++i) {
					query.setParameter(i - argsOffset + 1, args[i]);
				}
				
				if(resultsOffset > 0)
					query.setFirstResult(resultsOffset);
				
				if(resultsLimit != -1)
					query.setMaxResults(resultsLimit);
				
				if(noReturn) {
					query.executeUpdate();
					return null;
				} else {
					return multiple ? query.getResultList() : query.getSingleResult();
				}
			}
			
			// it should be one of the "usual" methods -> delegate it
			try {
				Class<?> delegatedClass = baseDao.getClass();
				Method delegated = delegatedClass.getMethod(method.getName(), method.getParameterTypes());
				return delegated.invoke(baseDao, args);
			} catch (NoSuchMethodException nsmEx) {
				throw new UnsupportedOperationException("Unable to find a strategy to auto-generate method " + method);
			} catch (InvocationTargetException itEx) {
				throw itEx.getCause();
			}
		}
	};

	private final Map<Class<?>, WeakReference<AbstractDAO<?, ?>>> cache;
	private final EntityManager entityManager;
	
	public DAOFactoryImpl(EntityManager em) {
		this.cache = new HashMap<Class<?>, WeakReference<AbstractDAO<?, ?>>>();
		this.entityManager = em;
	}
	
	@SuppressWarnings("unchecked")
	private <K, E, D extends AbstractDAO<K, E>> D getInstance(Class<E> entityClass, Class<D> daoClass) {
		D instance = cache.containsKey(daoClass) ? (D) cache.get(daoClass).get() : null;
		
		if(instance == null) {
			if(daoClass.isInterface()) {
				instance = (D) Proxy.newProxyInstance(daoClass.getClassLoader(), new Class<?>[] { daoClass }, 
						new ProxyInvocationHandler<K, E>(entityClass));
			} else {
				try {
					instance = daoClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("Cannot create DAO instance for class " + daoClass, e);
				}
			}
			cache.put(daoClass, new WeakReference<AbstractDAO<?, ?>>(instance));
		}
		
		return instance;
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <K, E> AbstractDAO<K, E> getDaoFor(Class<E> entityClass) {
		AbstractDAO instance = cache.containsKey(entityClass) ? cache.get(entityClass).get() : null;
		
		if(instance == null) {
			PersistedBy annotation = entityClass.getAnnotation(PersistedBy.class);
			if(annotation != null) {
				instance = getInstance(entityClass, (Class<? extends AbstractDAO<K, E>>) annotation.value());
			} else {
				try {
					Class<? extends AbstractDAO<K, E>> implicitDao = (Class<? extends AbstractDAO<K, E>>) Class.forName(entityClass.getName() + "$DAO");
					instance = getInstance(entityClass, implicitDao);
				} catch (Exception e) {}
			}
			
			if(instance == null) {
				instance = new GenericDAO(entityManager, entityClass);
			}
			cache.put(entityClass, new WeakReference(instance));
		}
		
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, E, D extends AbstractDAO<K, E>> D getDao(Class<D> daoClass) {
		Class<?> entityClass = null;
		Persists annotation = daoClass.getAnnotation(Persists.class);
		if(annotation != null) {
			entityClass = annotation.value();
		}
		
		if(entityClass == null) {
			entityClass = daoClass.getEnclosingClass();
		}
		
		if(entityClass == null)
			throw new IllegalStateException("Cannot discovery entity class relative to " + daoClass);
		
		return getInstance((Class<E>)entityClass, daoClass);
	}

	protected <K, E> AbstractDAO<K, E> createGenericImpl(EntityManager em, Class<E> entityClass) {
		return new GenericDAO<K, E>(em, entityClass);
	}

}
