package com.example.common.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;

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
}
