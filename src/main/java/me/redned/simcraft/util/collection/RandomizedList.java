package me.redned.simcraft.util.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class RandomizedList<E> implements List<E> {
    private final List<E> list;

    public RandomizedList(E... list) {
        this(new ArrayList<>(Arrays.asList(list)));
    }

    public RandomizedList(List<E> list) {
        this.list = list;

        Collections.shuffle(list);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        try {
            return this.list.iterator();
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public Object[] toArray() {
        try {
            return this.list.toArray();
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        try {
            return this.list.toArray(a);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public boolean add(E e) {
        try {
            return this.list.add(e);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public boolean remove(Object o) {
        try {
            return this.list.remove(o);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        try {
            return this.list.addAll(c);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        try {
            return this.list.addAll(index, c);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        try {
            return this.list.removeAll(c);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        try {
            return this.list.retainAll(c);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public E get(int index) {
        try {
            return this.list.get(index);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public E set(int index, E element) {
        try {
            return this.list.set(index, element);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public void add(int index, E element) {
        try {
            this.list.add(index, element);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public E remove(int index) {
        try {
            return this.list.remove(index);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public int indexOf(Object o) {
        try {
            return this.list.indexOf(o);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        try {
            return this.list.lastIndexOf(o);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        try {
            return this.list.listIterator();
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        try {
            return this.list.listIterator(index);
        } finally {
            Collections.shuffle(this.list);
        }
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        try {
            return this.list.subList(fromIndex, toIndex);
        } finally {
            Collections.shuffle(this.list);
        }
    }
}
