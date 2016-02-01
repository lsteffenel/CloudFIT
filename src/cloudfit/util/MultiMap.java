/* *************************************************************** *
 * PER-MARE Project (project number 13STIC07)
 * http://cosy.univ-reims.fr/PER-MARE
 * A CAPES/MAEE/ANII STIC-AmSud collaboration program.
 * All rights reserved to project partners:
 *  - Universite de Reims Champagne-Ardenne, Reims, France 
 *  - Universite Paris 1 Pantheon Sorbonne, Paris, France
 *  - Universidade Federal de Santa Maria, Santa Maria, Brazil
 *  - Universidad de la Republica, Montevideo, Uruguay
 * 
 * *************************************************************** *
 */
package cloudfit.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * MultiMap class proposes a particular kind of Map in which a key can be
 * related to multiple values.
 *
 * @author kirsch & lsteffenel
 * @param <K> Key type
 * @param <V> Values type
 */
public class MultiMap<K, V> implements Serializable, Cloneable, Map<K, V> {

    public static final short VALUES_INTIAL_CAPACITY = 5;
    private LinkedHashMap<K, Collection<V>> map;
    private int values_initial_capacity;

    /**
     * Constructs an empty MultiMap instance with the default initial capacity
     * for keys and for values (5).
     */
    public MultiMap() {
        this.values_initial_capacity = VALUES_INTIAL_CAPACITY;
        this.map = new LinkedHashMap<K, Collection<V>>();
    }

    /**
     * Constructs an empty MultiMap instance with the default initial capacity
     * for keys and the initial capacity indicated as parameter for the values.
     * This means that for each key, we are expecting about
     * <i>values_initial_capacity</i> values.
     *
     * @param values_initial_capacity initial capacity for the collection of
     * values per key.
     */
    public MultiMap(int values_initial_capacity) {
        this.values_initial_capacity = values_initial_capacity;
        this.map = new LinkedHashMap<K, Collection<V>>();
    }

    /**
     * Constructs an empty MultiMap instance with a given initial capacity for
     * keys and for the values collection per key. This means that we are
     * expecting initially <i>keys_initial_capacity</i> keys and, for each key,
     * we are expecting about <i>values_initial_capacity</i> values.
     *
     * @param values_initial_capacity initial capacity for the collection of
     * values per key.
     * @param keys_initial_capacity initial capacity for the keys.
     */
    public MultiMap(int values_initial_capacity, int keys_initial_capacity) {
        this.values_initial_capacity = values_initial_capacity;
        this.map = new LinkedHashMap<K, Collection<V>>(keys_initial_capacity);
    }

    /**
     * adds a new value to those associated to the key. More than one value can
     * be associated with one key.
     *
     * @param token key value
     * @param value value to be associated with the key
     */
    public void add(K token, V value) {
        Collection<V> values = map.get(token);
        if (values == null) {
            values = this.createCollection();
            map.put(token, values);
        }
        values.add(value);
    }

    /**
     * returns the set of keys available on the MultiMap
     *
     * @return Set&lt;K&gt; of tokens (keys) used in this Map.
     */
    public Set<K> getKeys() {
        return map.keySet();
    }

    /**
     * returns the <i>n-th</i> key (token) in the key set. Key position is
     * determined by the insertion order.
     *
     * @param pos key position in the key set
     * @return K key or null if no key in this position
     */
    public K getKey(int pos) {
        /* ** trying a faster code **/
        if (pos >= 0 && pos < size()) {
            Set<K> keys = map.keySet();
            K key = null;

            /*
             Iterator ikeys = keys.iterator();
             int count = 0;
             while (ikeys.hasNext() && count <= pos) {
             key = (K) ikeys.next();
             count++;
             }
             */
            //not sure that will have a better performance than iterating
            //more tests are needed
            key = (K) keys.toArray()[pos];
            return key;
        } else {
            return null;
        }
    }

    /**
     * return the number of keys (elements) on the MultiMap.
     *
     * @return size number of elements on the Map
     */
    public int size() {
        return map.size();
    }

    /**
     * returns all values associated with a given key. Note that a key may have
     * several values associated with it.
     *
     * @param key token whose values we are looking for
     * @return values associated with the key or null, if the key is not present
     * in the MultiMap.
     */
    public Collection<V> getValues(K key) {
        return map.get(key);
    }

    /**
     * Returns an iterator given access to the collection of values associated
     * with a given key.
     *
     * @param key token whose associated values is to be returned
     * @return iterator over values associated with the key
     */
    public Iterator keyIterator(K key) {
        return map.get(key).iterator();
    }

    /**
     * Returns true if this map contains no key-values mappings.
     *
     * @return true if MultiMap is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * Returns true if the map already contains the specified key.
     *
     * @param key token whose present is to be tested
     * @return true if map contain one or more values associated to this key,
     * false otherwise
     */
    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    /**
     * Returns true if at least one of the keys contains this value.
     *
     * @param value we are looking for
     * @return true if value is associated with at least one key in th map
     */
    @Override
    public boolean containsValue(Object value) {
        boolean found = false;

        Collection<Collection<V>> allValues = this.map.values();
        for (Collection<V> c : allValues) {
            if (c.contains(value)) {
                return true;
            }
        }

        return found;
    }

    /**
     * returns the first value V associated with the key. Note that other values
     * can be associated with this key.
     *
     * @param key - key whose value is to be returned
     * @return the first value associated with the key
     */
    @Override
    public V get(Object key) {
        V val = null;

        ArrayList<V> values = (ArrayList) this.map.get(key);
        if (values != null && values.size() > 0) {
            val = values.get(0);
        }

        return val;
    }

    /**
     * associates the specified value with a given key, <b>replacing</b> all
     * previous values by this one. Old values associated with this key are
     * returned as a Collection of values.
     *
     * @param key (token) with which the specified value is to be associated
     * @param value to be associated with the specified key
     * @return Collection<V> containing all values associated with this key.
     * @throws ClassCastException - if the class of the specified key or value
     * does not correspond to declared &lt;K,V&gt;, preventing it from being
     * stored in this map.
     */
    @Override
    public Object put(Object key, Object value) {
        //   public Object put(K key, V value) {
        Collection<V> values = this.createCollection();
        values.add((V) value);
        Collection<V> old = this.map.put((K) key, values);
        return old;
    }

    /**
     * removes from the map the key and its associated values.
     *
     * @param key whose mapping is to be removed from the map
     * @return V the first value associated with the key
     */
    @Override
    public V remove(Object key) {
        V val = null;
        ArrayList<V> values = (ArrayList) this.map.get(key);
        if (values != null && values.size() > 0) {
            val = values.get(0);
        }
        return val;
    }

    /**
     * Adds all elements from the Map m to this MultiMap.
     *
     * @throws ClassCastException if the class of a key or value in m does not
     * correspond to K or to V (&lt;? extends K, ? extends V&gt;), preventing it
     * from being stored in this map
     * @throws NullPointerException if the specified map is null
     *
     * @param m Map whose elements will be add to this one
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Set<? extends K> mkeys = m.keySet();
        for (K token : mkeys) {
            this.add(token, (V) m.get(token));
        }
    }

    /**
     * Adds all elements from the MultiMap m to this MultiMap.
     *
     * @param m MultiMap whose elements will be add to this one
     */
    public void putAll(MultiMap<K, V> m) {
        Set<K> mkeys = m.getKeys();
        for (K token : mkeys) {
            Collection<V> values = map.get(token);
            if (values == null) { //token wasn't a key in map
                values = this.createCollection(); //add it to the map
                map.put(token, values);
            }
            values.addAll(m.getValues(token)); //adding values 
        }
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    /**
     * Returns a Collection view of the values contained in this map.
     * <b>Note</b> that, since key may be associated with multiple values, all
     * values related to a key will be returned in a Collection, producing a
     * Collection containing itself other Collection of values (one Collection
     * by key). Besides, similar to other Map implementations, the returned
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.
     *
     * @return Collection &lt; Collection &lt; V &gt; &gt; containg values
     * associated to each key.
     */
    @Override
    public Collection values() {
        Collection<Collection<V>> values = this.map.values();
        return values;
    }

    /**
     * this opperation is unsupported.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public Set entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * creates the collection that will keep values for each key.
     *
     * @return Collection of V for key values
     */
    protected Collection<V> createCollection() {
        return new ArrayList<V>(this.values_initial_capacity);
    }
//    public MultiMap<K,V> clone()
//    {
//        MultiMap<K,V> copy = new MultiMap<K,V>();
//        copy.putAll(this);
//        return copy;
//    }
}