package ru.itmo.java;

public class HashTable {

    private class Entry
    {
        private int hashCode;

        private int next;

        private Object key;

        private Object value;

        private Entry(int hashCode, int next, Object key, Object value) {
            this.hashCode = hashCode;
            this.next = next;
            this.key = key;
            this.value = value;
        }
    }

    private static final int DEFAULT_CAPACITY = 13;

    private static final double DEFAULT_LOAD_FACTOR = 0.5;

    private static final int DEFAULT_ENTRY_INDEX = -1;

    private static final int POSITIVE_VALUE_MASK = 0x7FFFFFFF;

    private static final int RESIZE_FACTOR = 2;

    private int capacity;

    private double loadFactor;

    private int threshold;

    private int count;

    private int freeCount;

    private int freeList;

    private int[] buckets;

    private Entry[] entries;

    public HashTable()
    {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashTable(double loadFactor)
    {
        this(DEFAULT_CAPACITY, loadFactor);
    }

    public HashTable(int initialCapacity, double loadFactor)
    {
        capacity = initialCapacity;
        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
        buckets = new int[capacity];
        entries = new Entry[threshold];
        freeList = DEFAULT_ENTRY_INDEX;
        for(int i = 0; i < capacity; ++i)
            buckets[i] = DEFAULT_ENTRY_INDEX;
    }

    public Object put(Object key, Object value) {
        int hashCode = key.hashCode() & POSITIVE_VALUE_MASK;
        int targetBucket = hashCode % capacity;
        for (int i = buckets[targetBucket]; i >= 0; i = entries[i].next) {
            if (entries[i].hashCode == hashCode && entries[i].key.equals(key)) {
                Object oldValue = entries[i].value;
                entries[i].value = value;
                return oldValue;
            }
        }
        int index;
        if (freeCount > 0) {
            index = freeList;
            freeList = entries[index].next;
            freeCount--;
        } else {
            if (count == entries.length) {
                Resize(capacity * RESIZE_FACTOR);
                targetBucket = hashCode % buckets.length;
            }
            index = count;
            ++count;
        }
        entries[index] = new Entry(hashCode, buckets[targetBucket], key, value);
        buckets[targetBucket] = index;
        return null;
    }

    private void Resize(int newSize)
    {
        capacity = newSize;
        threshold = (int)(capacity * loadFactor);
        int[] newBuckets = new int[newSize];
        for (int i = 0; i < newBuckets.length; i++)
            newBuckets[i] = DEFAULT_ENTRY_INDEX;
        Entry[] newEntries = new Entry[threshold];
        System.arraycopy(entries, 0, newEntries, 0, count);
        for (int i = 0; i < count; i++) {
            if (newEntries[i].hashCode >= 0) {
                int bucket = newEntries[i].hashCode % newSize;
                newEntries[i].next = newBuckets[bucket];
                newBuckets[bucket] = i;
            }
        }
        buckets = newBuckets;
        entries = newEntries;
    }

    public Object get(Object key) {
        int hashCode = key.hashCode() & POSITIVE_VALUE_MASK;
        for (int i = buckets[hashCode % buckets.length]; i >= 0; i = entries[i].next) {
            if (entries[i].hashCode == hashCode && entries[i].key.equals(key))
                return entries[i].value;
        }
        return null;
    }

    public Object remove(Object key) {
        int hashCode = key.hashCode() & POSITIVE_VALUE_MASK;
        int bucket = hashCode % buckets.length;
        int last = -1;
        for (int i = buckets[bucket]; i >= 0; last = i, i = entries[i].next) {
            if (entries[i].hashCode == hashCode && entries[i].key.equals(key)) {
                if (last < 0) {
                    buckets[bucket] = entries[i].next;
                }
                else {
                    entries[last].next = entries[i].next;
                }
                Object oldValue = entries[i].value;
                entries[i] = new Entry(DEFAULT_ENTRY_INDEX, freeList, null, null);
                freeList = i;
                freeCount++;
                return oldValue;
            }
        }
        return null;
    }

    public int size() {
        return count - freeCount;
    }

}
