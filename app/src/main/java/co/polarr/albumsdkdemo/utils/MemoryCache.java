package co.polarr.albumsdkdemo.utils;

import android.util.LruCache;

/**
 * Created by Colin on 2017/2/22.
 * Sample memory cache for transforming data between activities
 */

public class MemoryCache {
    private static LruCache<String, Object> cache = new LruCache<>(10);

    public static void put(String key, Object value) {
        if(value == null) {
            return;
        }
        cache.put(key, value);
    }

    public static Object get(String key) {
        return cache.get(key);
    }

    public static void remove(String key) {
        cache.remove(key);
    }

    public static Object pick(String key) {
        Object result = get(key);
        remove(key);
        return result;
    }
}
