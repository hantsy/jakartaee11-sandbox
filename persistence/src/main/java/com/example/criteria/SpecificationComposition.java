package com.example.criteria;

import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;

class SpecificationComposition {

    interface Combiner extends Serializable {
        Predicate combine(CriteriaBuilder builder, @Nullable Predicate lhs, @Nullable Predicate rhs);
    }

    static <T> Specification<T> composed(@Nullable Specification<T> lhs, @Nullable Specification<T> rhs,
                                         Combiner combiner) {

        return (root, query, builder) -> {

            Predicate thisPredicate = toPredicate(lhs, root, query, builder);
            Predicate otherPredicate = toPredicate(rhs, root, query, builder);

            if (thisPredicate == null) {
                return otherPredicate;
            }

            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
        };
    }

    @Nullable
    private static <T> Predicate toPredicate(@Nullable Specification<T> specification, Root<T> root, @Nullable CriteriaQuery<?> query,
                                             CriteriaBuilder builder) {
        return specification == null ? null : specification.toPredicate(root, query, builder);
    }
}
