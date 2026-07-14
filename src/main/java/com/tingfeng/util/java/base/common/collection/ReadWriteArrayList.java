package com.tingfeng.util.java.base.common.collection;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * 高性能读写列表，使用 ReadWriteLock 实现读并发、写独占。
 *
 * 适用于多读少写场景：读操作可并发提升性能，写操作保持独占。
 *
 * @param <E> 元素类型
 */
public class ReadWriteArrayList<E> extends AbstractList<E> implements List<E> {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final List<E> list = new ArrayList<>();

    public ReadWriteArrayList() {
        super();
    }

    public ReadWriteArrayList(Collection<? extends E> c) {
        super();
        list.addAll(c);
    }

    @Override
    public E get(int index) {
        rwLock.readLock().lock();
        try {
            return list.get(index);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        rwLock.readLock().lock();
        try {
            return list.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        rwLock.readLock().lock();
        try {
            return list.isEmpty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        rwLock.readLock().lock();
        try {
            return list.contains(o);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private int cursor = 0;
            private int lastReturned = -1;

            @Override
            public boolean hasNext() {
                rwLock.readLock().lock();
                try {
                    return cursor < list.size();
                } finally {
                    rwLock.readLock().unlock();
                }
            }

            @Override
            public E next() {
                rwLock.readLock().lock();
                try {
                    if (cursor >= list.size()) {
                        throw new NoSuchElementException();
                    }
                    lastReturned = cursor;
                    return list.get(cursor++);
                } finally {
                    rwLock.readLock().unlock();
                }
            }

            @Override
            public void remove() {
                if (lastReturned < 0) {
                    throw new IllegalStateException();
                }
                rwLock.writeLock().lock();
                try {
                    list.remove(lastReturned);
                    cursor = lastReturned;
                    lastReturned = -1;
                    modCount++;
                } finally {
                    rwLock.writeLock().unlock();
                }
            }
        };
    }

    @Override
    public Spliterator<E> spliterator() {
        rwLock.readLock().lock();
        try {
            return Spliterators.spliteratorUnknownSize(
                    new ArrayList<>(list).iterator(),
                    Spliterator.ORDERED
            );
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Object[] toArray() {
        rwLock.readLock().lock();
        try {
            return list.toArray();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        rwLock.readLock().lock();
        try {
            return (T[]) list.toArray(a);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean add(E e) {
        rwLock.writeLock().lock();
        try {
            boolean result = list.add(e);
            modCount++;
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        rwLock.writeLock().lock();
        try {
            boolean result = list.remove(o);
            if (result) {
                modCount++;
            }
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        rwLock.readLock().lock();
        try {
            return list.containsAll(c);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        rwLock.writeLock().lock();
        try {
            boolean result = list.addAll(c);
            if (result) {
                modCount++;
            }
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        rwLock.writeLock().lock();
        try {
            boolean result = list.addAll(index, c);
            if (result) {
                modCount++;
            }
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        rwLock.writeLock().lock();
        try {
            boolean result = list.removeAll(c);
            if (result) {
                modCount++;
            }
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        rwLock.writeLock().lock();
        try {
            boolean result = list.retainAll(c);
            if (result) {
                modCount++;
            }
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        rwLock.writeLock().lock();
        try {
            boolean modified = false;
            for (int i = list.size() - 1; i >= 0; i--) {
                if (filter.test(list.get(i))) {
                    list.remove(i);
                    modified = true;
                }
            }
            if (modified) {
                modCount++;
            }
            return modified;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        rwLock.writeLock().lock();
        try {
            list.clear();
            modCount++;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E set(int index, E element) {
        rwLock.writeLock().lock();
        try {
            return list.set(index, element);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void add(int index, E element) {
        rwLock.writeLock().lock();
        try {
            list.add(index, element);
            modCount++;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public E remove(int index) {
        rwLock.writeLock().lock();
        try {
            E result = list.remove(index);
            modCount++;
            return result;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int indexOf(Object o) {
        rwLock.readLock().lock();
        try {
            return list.indexOf(o);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        rwLock.readLock().lock();
        try {
            return list.lastIndexOf(o);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(list).listIterator();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(list).listIterator(index);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        rwLock.readLock().lock();
        try {
            // 返回独立快照副本，对原列表的后续修改不影响 subList
            return new ArrayList<>(list.subList(fromIndex, toIndex));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void sort(Comparator<? super E> c) {
        rwLock.writeLock().lock();
        try {
            list.sort(c);
            modCount++;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(list).hashCode();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(list).equals(o);
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
