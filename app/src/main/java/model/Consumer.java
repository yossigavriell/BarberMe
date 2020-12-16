package model;

public interface Consumer<T> {
    public void apply(T param);
}
