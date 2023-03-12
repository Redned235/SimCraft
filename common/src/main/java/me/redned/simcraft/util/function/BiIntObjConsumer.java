package me.redned.simcraft.util.function;

@FunctionalInterface
public interface BiIntObjConsumer<V> {

    void accept(int x, int y, V value);
}
