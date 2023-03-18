package me.redned.simcraft.util.collection;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import org.cloudburstmc.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ThreeDimensionalPositionList implements List<Vector3i> {
    private final LongList backingList = new LongArrayList();

    @Override
    public int size() {
        return this.backingList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backingList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Long l) {
            return this.backingList.contains(l.longValue());
        } else if (o instanceof Vector3i vec) {
            return this.backingList.contains(serialize(vec.getX(), vec.getY(), vec.getZ()));
        }

        return false;
    }

    public boolean contains(int x, int y, int z) {
        return this.backingList.contains(serialize(x, y, z));
    }

    @Override
    public Iterator<Vector3i> iterator() {
        LongListIterator backingIterator = this.backingList.iterator();
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return backingIterator.hasNext();
            }

            @Override
            public Vector3i next() {
                long l = backingIterator.nextLong();
                return Vector3i.from(getX(l), getY(l), getZ(l));
            }

            @Override
            public void remove() {
                backingIterator.remove();
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] backingObjects = this.backingList.toArray();
        Object[] objects = new Object[backingObjects.length];
        for (int i = 0; i < objects.length; i++) {
            Object backingObject = backingObjects[i];
            if (backingObject instanceof Long l) {
                objects[i] = Vector3i.from(getX(l), getY(l), getZ(l));
            } else if (backingObject instanceof Vector3i vec) {
                objects[i] = vec;
            }
        }

        return objects;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a == null) {
            return (T[]) new Object[0];
        }

        Class<?> type = a.getClass().getComponentType();
        if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            return (T[]) this.backingList.toArray(Long[]::new);
        }

        if (type.isAssignableFrom(Vector3i.class)) {
            long[] backingArray = this.backingList.toLongArray();
            T[] array = (T[]) new Object[backingArray.length];
            for (int i = 0; i < backingArray.length; i++) {
                long l = backingArray[i];
                array[i] = (T) Vector3i.from(getX(l), getY(l), getZ(l));
            }

            return array;
        }

        return (T[]) new Object[0];
    }

    @Override
    public boolean add(Vector3i v) {
        return this.backingList.add(serialize(v.getX(), v.getY(), v.getZ()));
    }

    public boolean add(int x, int y, int z) {
        return this.backingList.add(serialize(x, y, z));
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Long l) {
            this.backingList.rem(l);
        } else if (o instanceof Vector3i vec) {
            this.backingList.rem(serialize(vec.getX(), vec.getY(), vec.getZ()));
        }

        return false;
    }

    public boolean remove(int x, int y, int z) {
        return this.backingList.rem(serialize(x, y, z));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        List<Object> list = new ArrayList<>(c.size());
        for (Object o : c) {
            if (o instanceof Vector3i vec) {
                list.add(serialize(vec.getX(), vec.getY(), vec.getZ()));
            } else {
                list.add(o);
            }
        }

        return this.backingList.containsAll(list);
    }

    @Override
    public boolean addAll(Collection<? extends Vector3i> c) {
        return this.backingList.addAll(c.stream()
                .map(vec -> serialize(vec.getX(), vec.getY(), vec.getZ()))
                .toList()
        );
    }

    @Override
    public boolean addAll(int index, Collection<? extends Vector3i> c) {
        return this.backingList.addAll(index, c.stream()
                .map(vec -> serialize(vec.getX(), vec.getY(), vec.getZ()))
                .toList()
        );
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            this.remove(o);
        }

        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<Object> list = new ArrayList<>(c.size());
        for (Object o : c) {
            if (o instanceof Vector3i vec) {
                list.add(serialize(vec.getX(), vec.getY(), vec.getZ()));
            } else {
                list.add(o);
            }
        }

        return this.backingList.retainAll(list);
    }

    @Override
    public void clear() {
        this.backingList.clear();
    }

    @Override
    public Vector3i get(int index) {
        long l = this.backingList.getLong(index);
        return Vector3i.from(getX(l), getY(l), getZ(l));
    }

    @Override
    public Vector3i set(int index, Vector3i element) {
        long l = this.backingList.set(index, serialize(element.getX(), element.getY(), element.getZ()));
        return Vector3i.from(getX(l), getY(l), getZ(l));
    }

    @Override
    public void add(int index, Vector3i element) {
        this.backingList.add(index, serialize(element.getX(), element.getY(), element.getZ()));
    }

    @Override
    public Vector3i remove(int index) {
        long l = this.backingList.removeLong(index);
        return Vector3i.from(getX(l), getY(l), getZ(l));
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof Long l) {
            return this.backingList.indexOf(l.longValue());
        } else if (o instanceof Vector3i vec) {
            return this.backingList.indexOf(serialize(vec.getX(), vec.getY(), vec.getZ()));
        }

        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o instanceof Long l) {
            return this.backingList.lastIndexOf(l.longValue());
        } else if (o instanceof Vector3i vec) {
            return this.backingList.lastIndexOf(serialize(vec.getX(), vec.getY(), vec.getZ()));
        }

        return 0;
    }

    @Override
    public ListIterator<Vector3i> listIterator() {
        LongListIterator backingIterator = this.backingList.listIterator();
        return this.fromBackingIterator(backingIterator);
    }

    @Override
    public ListIterator<Vector3i> listIterator(int index) {
        LongListIterator backingIterator = this.backingList.listIterator(index);
        return this.fromBackingIterator(backingIterator);
    }

    @Override
    public List<Vector3i> subList(int fromIndex, int toIndex) {
        LongList backingSubList = this.backingList.subList(fromIndex, toIndex);
        List<Vector3i> subList = new ArrayList<>(backingSubList.size());
        backingSubList.forEach(l -> {
            subList.add(Vector3i.from(getX(l), getY(l), getZ(l)));
        });

        return subList;
    }

    private ListIterator<Vector3i> fromBackingIterator(LongListIterator backingIterator) {
        return new ListIterator<>() {
            @Override
            public boolean hasNext() {
                return backingIterator.hasNext();
            }

            @Override
            public Vector3i next() {
                long l = backingIterator.nextLong();
                return Vector3i.from(getX(l), getY(l), getZ(l));
            }

            @Override
            public boolean hasPrevious() {
                return backingIterator.hasPrevious();
            }

            @Override
            public Vector3i previous() {
                long l = backingIterator.previousLong();
                return Vector3i.from(getX(l), getY(l), getZ(l));
            }

            @Override
            public int nextIndex() {
                return backingIterator.nextIndex();
            }

            @Override
            public int previousIndex() {
                return backingIterator.previousIndex();
            }

            @Override
            public void remove() {
                backingIterator.remove();
            }

            @Override
            public void set(Vector3i vector3i) {
                backingIterator.set(serialize(getX(vector3i.getX()), getY(vector3i.getY()), getZ(vector3i.getZ())));
            }

            @Override
            public void add(Vector3i vector3i) {
                backingIterator.add(serialize(getX(vector3i.getX()), getY(vector3i.getY()), getZ(vector3i.getZ())));
            }
        };
    }

    private static long serialize(final int x, final int y, float z) {
        return (((long) x & 0x03FFFFFFL) << 38) | (((long) y & 0xFFFL)) | (((long) z & 0x03FFFFFFL) << 12);
    }

    private static int getX(final long serialized) {
        return (int) (serialized >> 38);
    }

    private static int getY(final long serialized) {
        return (int) ((serialized << 52) >> 52);
    }

    private static int getZ(final long serialized) {
        return (int) ((serialized << 26) >> 38);
    }
}
