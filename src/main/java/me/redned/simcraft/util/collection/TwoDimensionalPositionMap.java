package me.redned.simcraft.util.collection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.redned.simcraft.util.function.BiIntObjConsumer;
import org.cloudburstmc.math.vector.Vector2i;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TwoDimensionalPositionMap<V> implements Map<Vector2i, V> {
    private final Long2ObjectMap<V> backingMap = new Long2ObjectOpenHashMap<>();

    @Override
    public int size() {
        return this.backingMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backingMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof Long) {
            return this.backingMap.containsKey(key);
        } else if (key instanceof Vector2i vec) {
            return this.backingMap.containsKey(serialize(vec.getX(), vec.getY()));
        }

        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.backingMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof Long) {
            return this.backingMap.get(key);
        } else if (key instanceof Vector2i vec) {
            return this.backingMap.get(serialize(vec.getX(), vec.getY()));
        }

        return null;
    }

    public V get(int x, int y) {
        return this.backingMap.get(serialize(x, y));
    }

    @Override
    public V put(Vector2i key, V value) {
        return this.backingMap.put(serialize(key.getX(), key.getY()), value);
    }

    public V put(int x, int y, V value) {
        return this.backingMap.put(serialize(x, y), value);
    }

    @Override
    public V remove(Object key) {
        if (key instanceof Long) {
            this.backingMap.remove(key);
        } else if (key instanceof Vector2i vec) {
            this.backingMap.remove(serialize(vec.getX(), vec.getY()));
        }

        return null;
    }

    public V remove(int x, int y) {
        return this.backingMap.remove(serialize(x, y));
    }

    @Override
    public void putAll(Map<? extends Vector2i, ? extends V> m) {
        for (Entry<? extends Vector2i, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.backingMap.clear();
    }

    @Override
    public Set<Vector2i> keySet() {
        Set<Long> backingKeySet = this.backingMap.keySet();
        Set<Vector2i> keys = new HashSet<>(backingKeySet.size());
        for (Long value : backingKeySet) {
            keys.add(Vector2i.from(getX(value), getY(value)));
        }

        return keys;
    }

    @Override
    public Collection<V> values() {
        return this.backingMap.values();
    }

    @Override
    public Set<Entry<Vector2i, V>> entrySet() {
        Set<Long2ObjectMap.Entry<V>> backingEntrySet = this.backingMap.long2ObjectEntrySet();
        Set<Entry<Vector2i, V>> entries = new HashSet<>(backingEntrySet.size());
        for (Long2ObjectMap.Entry<V> entry : backingEntrySet) {
            long longKey = entry.getLongKey();
            entries.add(new Map.Entry<>() {
                private final Vector2i key = Vector2i.from(getX(longKey), getY(longKey));
                private final V value = entry.getValue();

                @Override
                public Vector2i getKey() {
                    return this.key;
                }

                @Override
                public V getValue() {
                    return this.value;
                }

                @Override
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
            });
        }

        return entries;
    }

    public void forEach(BiIntObjConsumer<? super V> consumer) {
        Objects.requireNonNull(consumer);

        for (Long2ObjectMap.Entry<V> entry : this.backingMap.long2ObjectEntrySet()) {
            long k;
            V v;
            try {
                k = entry.getLongKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }

            consumer.accept(getX(k), getY(k), v);
        }
    }

    private static long serialize(final int x, final int y) {
        return ((long) y << 32) | (x & 0xFFFFFFFFL);
    }

    private static int getX(final long serialized) {
        return (int) serialized;
    }

    private static int getY(final long serialized) {
        return (int) (serialized >>> 32);
    }
}
