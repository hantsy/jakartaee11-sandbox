package com.example.spec;

import jakarta.persistence.EntityManager;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public interface Repository<T, K> {

    EntityManager entityManager();

    private Class<T> entityClazz() {
        // get Entity type from the first parameter of Repository interface
        Type[] types = this.getClass().getGenericInterfaces();
        for (Type type : types) {
            if (type instanceof ParameterizedType pt && type.getTypeName().equals(Repository.class.getName())) {
                return (Class<T>) pt.getActualTypeArguments()[0];
            }
        }

        throw new IllegalArgumentException("The class should implements Repository<T,K>");
    }

    default Stream<T> findBy(Specification<T> specification) {
        var criteriaBuilder = entityManager().getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(entityClazz());
        var root = query.from(entityClazz());

        var predicate = specification.toPredicate(root, query, criteriaBuilder);
        query.where(predicate);
        return entityManager().createQuery(query).getResultStream();
    }

    default Stream<T> findBy(Specification<T> specification, int offset, int limit) {
        var criteriaBuilder = entityManager().getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(entityClazz());
        var root = query.from(entityClazz());

        var predicate = specification.toPredicate(root, query, criteriaBuilder);
        query.where(predicate);
        return entityManager().createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultStream();
    }

    default long countBy(Specification<T> specification) {
        var criteriaBuilder = entityManager().getCriteriaBuilder();
        var query = criteriaBuilder.createQuery(Long.class);
        var root = query.from(entityClazz());

        query.select(criteriaBuilder.count(root));

        var predicate = specification.toPredicate(root, query, criteriaBuilder);
        query.where(predicate);
        return entityManager().createQuery(query).getSingleResult();
    }
}
