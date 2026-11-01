package com.brandonkamga.sourcefolio.profile.domain.model;

import java.util.HashSet;
import java.util.List;

/**
 * The specialties a member names, most important first: none until answered, then one to
 * three, each at most once.
 */
public record Specialties(List<Specialty> ranked) {

    public static final int MAX = 3;

    public Specialties {
        ranked = List.copyOf(ranked);
        if (ranked.size() > MAX) {
            throw new IllegalArgumentException("At most " + MAX + " specialties, got " + ranked.size());
        }
        if (new HashSet<>(ranked).size() != ranked.size()) {
            throw new IllegalArgumentException("A specialty is named twice: " + ranked);
        }
    }

    public static Specialties none() {
        return new Specialties(List.of());
    }

    public boolean isEmpty() {
        return ranked.isEmpty();
    }

    public List<String> slugs() {
        return ranked.stream().map(Specialty::slug).toList();
    }
}
