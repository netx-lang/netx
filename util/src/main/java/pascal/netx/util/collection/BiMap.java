package pascal.netx.util.collection;

import java.util.HashMap;
import java.util.Map;

public class BiMap<K, V> {
    private final Map<K, V> keyToValue = new HashMap<>();
    private final Map<V, K> valueToKey = new HashMap<>();

    public V getValue(K key) {
        return keyToValue.get(key);
    }

    public K getKey(V value) {
        return valueToKey.get(value);
    }

    public void put(K key, V value) {
        keyToValue.put(key, value);
        valueToKey.put(value, key);
    }

    public V removeKey(K key) {
        V value = keyToValue.remove(key);
        valueToKey.remove(value);
        return value;
    }

    public K removeValue(V value) {
        K key = valueToKey.remove(value);
        keyToValue.remove(key);
        return key;
    }
}
