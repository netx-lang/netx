package pascal.hardscript.util.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BiMapTest {
    @Test
    void putAndGetValue() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        assertEquals(1, biMap.getValue("one"));
    }

    @Test
    void putAndGetKey() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        assertEquals("one", biMap.getKey(1));
    }

    @Test
    void removeKey() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        assertEquals(1, biMap.removeKey("one"));
        assertNull(biMap.getValue("one"));
        assertNull(biMap.getKey(1));
    }

    @Test
    void removeValue() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        assertEquals("one", biMap.removeValue(1));
        assertNull(biMap.getValue("one"));
        assertNull(biMap.getKey(1));
    }

    @Test
    void getValueForNonExistentKey() {
        BiMap<String, Integer> biMap = new BiMap<>();
        assertNull(biMap.getValue("nonexistent"));
    }

    @Test
    void getKeyForNonExistentValue() {
        BiMap<String, Integer> biMap = new BiMap<>();
        assertNull(biMap.getKey(999));
    }
}