/*
 * Copyright (c) 2015 Sillelien
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package sillelien.tutum;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A very simple cache using java.util.concurrent. <p/> User: treeder, innovativeravi Date: Mar 15, 2009 Time: 8:42:01
 * PM
 */
public class SuperSimpleCache<K, V> {

    private ConcurrentHashMap<K, CacheEntry<V>> cache;
    /**
     * Used to restrict the size of the cache map.
     */
    private Queue<K> queue;
    private int maxSize;
    /**
     * Using this integer because ConcurrentLinkedQueue.size is not constant time.
     */
    private AtomicInteger cacheSize = new AtomicInteger();

    public SuperSimpleCache(int maxSize) {
        this.maxSize = maxSize;
        cache = new ConcurrentHashMap<K, CacheEntry<V>>(maxSize);
        queue = new ConcurrentLinkedQueue<K>();
    }


    public V getOrCreate(K key, int ttl, Callable<V> creator) {
        final V value = get(key);
        if(value != null) {
            return value;
        }
        final CacheEntry<V> cacheEntry = cache.computeIfAbsent(key, k -> {
            try {
                queue.add(key);
                return new CacheEntry<V>(ttl, creator.call());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        if(cacheEntry == null) {
            return null;
        }
        return cacheEntry.getEntry();
    }

    /**
     * {@inheritDoc}
     */
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("Invalid Key.");
        }

        CacheEntry<V> entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        long timestamp = entry.getExpireBy();
        if (timestamp != -1 && System.currentTimeMillis() > timestamp) {
            remove(key);
            return null;
        }
        return entry.getEntry();
    }

    /**
     * @param key
     */
    public V removeAndGet(K key) {

        if (key == null) {
            return null;
        }

        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            cacheSize.decrementAndGet();
            return cache.remove(key).getEntry();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void put(K key, V value, int secondsToLive) {
        if (key == null) {
            throw new IllegalArgumentException("Invalid Key.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Invalid Value.");
        }

        long expireBy = secondsToLive != -1 ? System.currentTimeMillis()
                                              + (secondsToLive * 1000) : secondsToLive;
        boolean exists = cache.containsKey(key);
        if (!exists) {
            cacheSize.incrementAndGet();
            while (cacheSize.get() > maxSize) {
                remove(queue.poll());
            }
        }
        cache.put(key, new CacheEntry<V>(expireBy, value));
        queue.add(key);
    }

    /**
     * Returns boolean to stay compatible with ehcache and memcached.
     *
     * @see #removeAndGet for alternate version.
     */
    public boolean remove(K key) {
        return removeAndGet(key) != null;
    }

    public int size() {
        return cacheSize.get();
    }

    /**
     * @param collection
     * @return
     */
    public Map<K, V> getAll(Collection<K> collection) {
        Map<K, V> ret = new HashMap<K, V>();
        for (K o : collection) {
            ret.put(o, get(o));
        }
        return ret;
    }

    public void clear() {
        cache.clear();
    }

    public int mapSize() {
        return cache.size();
    }

    public int queueSize() {
        return queue.size();
    }

    private class CacheEntry<V> {
        private long expireBy;
        private V entry;

        public CacheEntry(long expireBy, V entry) {
            super();
            this.expireBy = expireBy;
            this.entry = entry;
        }

        /**
         * @return the expireBy
         */
        public long getExpireBy() {
            return expireBy;
        }

        /**
         * @return the entry
         */
        public V getEntry() {
            return entry;
        }

    }

}