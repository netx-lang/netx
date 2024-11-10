package pascal.hardscript.util.collection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnionFindTest {
    @Test
    void testAbsentElement() {
        UnionFind<Integer> unionFind = new UnionFind<>();
        unionFind.add(114);
        unionFind.add(514);
        assertThrows(IllegalArgumentException.class,
                () -> unionFind.find(1919));
        assertEquals(2, unionFind.elementSize());
        assertEquals(2, unionFind.representatives().size());

        unionFind.union(1919, 514);
        assertEquals(unionFind.find(514), unionFind.find(1919));
        assertEquals(3, unionFind.elementSize());
        assertEquals(2, unionFind.representatives().size());

        unionFind.add(514);
        assertEquals(3, unionFind.elementSize());
        assertEquals(2, unionFind.representatives().size());
    }
}