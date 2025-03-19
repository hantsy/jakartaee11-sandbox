package com.example.criteria;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.StreamSupport;

@FunctionalInterface
public interface Specification<T> extends Serializable {
    @Serial
    long serialVersionUID = 1L;

    @Nullable
    Predicate toPredicate(Root<T> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder);

    static <T> Specification<T> not(@Nullable Specification<T> spec) {

        return spec == null //
                ? (root, query, builder) -> null //
                : (root, query, builder) -> builder.not(spec.toPredicate(root, query, builder));
    }

    default Specification<T> and(@Nullable Specification<T> other) {
        return SpecificationComposition.composed(this, other, CriteriaBuilder::and);
    }

    default Specification<T> or(@Nullable Specification<T> other) {
        return SpecificationComposition.composed(this, other, CriteriaBuilder::or);
    }

    static <T> Specification<T> where(@Nullable Specification<T> spec) {
        return spec == null ? (root, query, builder) -> null : spec;
    }

    static <T> Specification<T> allOf(Iterable<Specification<T>> specifications) {

        return StreamSupport.stream(specifications.spliterator(), false) //
                .reduce(Specification.where(null), Specification::and);
    }


    @SafeVarargs
    static <T> Specification<T> allOf(Specification<T>... specifications) {
        return allOf(Arrays.asList(specifications));
    }


    static <T> Specification<T> anyOf(Iterable<Specification<T>> specifications) {

        return StreamSupport.stream(specifications.spliterator(), false) //
                .reduce(Specification.where(null), Specification::or);
    }

        @SafeVarargs
    static <T> Specification<T> anyOf(Specification<T>... specifications) {
        return anyOf(Arrays.asList(specifications));
    }

}
