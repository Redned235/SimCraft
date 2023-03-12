package me.redned.simcraft.util.function;

@FunctionalInterface
public interface TriIntObjConsumer<V> {

    void accept(int x, int y, int z, V value);
}
