package com.github.gabert;

public class RingBuffer<T> {
    private final T[] data;
    private int position = 0;
    private final int capacity;

    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.data = (T[]) new Object[this.capacity];
    }

    public void offer(T element) {
        data[position] = element;
        position = (++position % capacity);
    }

    public T take() {
        return data[position];
    }
}
