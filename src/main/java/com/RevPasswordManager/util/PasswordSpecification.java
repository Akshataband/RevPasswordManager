package com.RevPasswordManager.util;

import com.RevPasswordManager.entities.PasswordEntry;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PasswordSpecification {

    public static Specification<PasswordEntry> filter(
            String search,
            String category,
            Long userId
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Only logged-in user data
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (search != null && !search.isEmpty()) {
                Predicate p1 = cb.like(cb.lower(root.get("accountName")), "%" + search.toLowerCase() + "%");
                Predicate p2 = cb.like(cb.lower(root.get("website")), "%" + search.toLowerCase() + "%");
                Predicate p3 = cb.like(cb.lower(root.get("username")), "%" + search.toLowerCase() + "%");

                predicates.add(cb.or(p1, p2, p3));
            }

            if (category != null && !category.isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}