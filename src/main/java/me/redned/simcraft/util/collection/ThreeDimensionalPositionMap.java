package me.redned.simcraft.util.collection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.redned.simcraft.util.function.TriIntObjConsumer;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ThreeDimensionalPositionMap<V> implements Map<Vector3i, V> {
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
        } else if (key instanceof Vector3i vec) {
            return this.backingMap.containsKey(serialize(vec.getX(), vec.getY(), vec.getZ()));
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
        } else if (key instanceof Vector3i vec) {
            return this.backingMap.get(serialize(vec.getX(), vec.getY(), vec.getZ()));
        }

        return null;
    }

    public V get(int x, int y, int z) {
        return this.backingMap.get(serialize(x, y, z));
    }

    @Override
    public V put(Vector3i key, V value) {
        return this.backingMap.put(serialize(key.getX(), key.getY(), key.getZ()), value);
    }

    public V put(int x, int y, int z, V value) {
        return this.backingMap.put(serialize(x, y, z), value);
    }

    @Override
    public V remove(Object key) {
        if (key instanceof Long) {
            this.backingMap.remove(key);
        } else if (key instanceof Vector3i vec) {
            this.backingMap.remove(serialize(vec.getX(), vec.getY(), vec.getZ()));
        }

        return null;
    }

    public V remove(int x, int y, int z) {
        return this.backingMap.remove(serialize(x, y, z));
    }

    @Override
    public void putAll(Map<? extends Vector3i, ? extends V> m) {
        for (Entry<? extends Vector3i, ? extends V> entry : m.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.backingMap.clear();
    }

    @Override
    public Set<Vector3i> keySet() {
        Set<Long> backingKeySet = this.backingMap.keySet();
        Set<Vector3i> keys = new HashSet<>(backingKeySet.size());
        for (Long value : backingKeySet) {
            keys.add(Vector3i.from(getX(value), getY(value), getZ(value)));
        }

        return keys;
    }

    @Override
    public Collection<V> values() {
        return this.backingMap.values();
    }

    @Override
    public Set<Entry<Vector3i, V>> entrySet() {
        Set<Long2ObjectMap.Entry<V>> backingEntrySet = this.backingMap.long2ObjectEntrySet();
        Set<Entry<Vector3i, V>> entries = new HashSet<>(backingEntrySet.size());
        for (Long2ObjectMap.Entry<V> entry : backingEntrySet) {
            long longKey = entry.getLongKey();
            entries.add(new Entry<>() {
                private final Vector3i key = Vector3i.from(getX(longKey), getY(longKey), getZ(longKey));
                private final V value = entry.getValue();

                @Override
                public Vector3i getKey() {
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

    public void forEach(TriIntObjConsumer<? super V> consumer) {
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

            consumer.accept(getX(k), getY(k), getZ(k), v);
        }
    }

    private static long serialize(final int x, final int y, float z) {
        return (((long) x & 0x03FFFFFFL) << 38) | (((long) y & 0xFFFL)) | (((long) z & 0x03FFFFFFL) << 12);
    }

    public static int getX(final long serialized) {
        return (int) (serialized >> 38);
    }

    public static int getY(final long serialized) {
        return (int) ((serialized << 52) >> 52);
    }

    public static int getZ(final long serialized) {
        return (int) ((serialized << 26) >> 38);
    }
}
