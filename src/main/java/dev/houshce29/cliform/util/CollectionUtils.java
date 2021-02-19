package dev.houshce29.cliform.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <I, T> Map<I, T> toIdentityMap(Collection<T> collection, Function<T, I> idMapper) {
        Map<I, T> map = new ConcurrentHashMap<>();
        for (T item : collection) {
            map.put(idMapper.apply(item), item);
        }
        return Collections.unmodifiableMap(map);
    }

    public static boolean isArrayEmpty(Object[] obj) {
        return obj == null || obj.length == 0;
    }
}
