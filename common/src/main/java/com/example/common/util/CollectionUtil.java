package com.example.common.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility class for common collection operations and validations.
 * Contains only methods that are actually used in the codebase.
 */
@UtilityClass
public class CollectionUtil {

    /**
     * Check if collection is null or empty
     * @param collection The collection to check
     * @return true if collection is null or empty, false otherwise
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if collection is not null and not empty
     * @param collection The collection to check
     * @return true if collection is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Calculate sum of numeric values from collection elements
     * @param collection The collection to process
     * @param mapper Function to extract numeric value from each element
     * @param <T> Type of collection elements
     * @return Sum of all values, 0.0 if collection is null or empty
     */
    public static <T> double sum(Collection<T> collection, Function<T, Double> mapper) {
        if (isEmpty(collection)) {
            return 0.0;
        }
        return collection.stream()
                .mapToDouble(mapper::apply)
                .filter(Objects::nonNull)
                .sum();
    }

}
