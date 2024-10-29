package pascal.hardscript.util.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitVectorUnitTest {

    @Test
    void parse() {
        BitVector bv1 = BitVector.parse("01101011", 7, 2);
        BitVector bv2 = BitVector.parse("01101011", 8, 2);
        BitVector bv3 = BitVector.parse("110101001101101011", 18, 2);
        BitVector bv4 = BitVector.parse("333", 8, 8);
        BitVector bv5 = BitVector.parse("333", 9, 8);
        BitVector bv6 = BitVector.parse("95", 8, 10);
        BitVector bv7 = BitVector.parse("95", 20, 10);
        BitVector bv8 = BitVector.parse("af41", 16, 16);
        BitVector bv9 = BitVector.parse("41", 16, 16);
        BitVector bv10 = BitVector.parse("41", 4, 16);
        assertEquals("1101011", bv1.toString());
        assertEquals("01101011", bv2.toString());
        assertEquals("110101001101101011", bv3.toString());
        assertEquals("11011011", bv4.toString());
        assertEquals("011011011", bv5.toString());
        assertEquals("01011111", bv6.toString());
        assertEquals("00000000000001011111", bv7.toString());
        assertEquals("1010111101000001", bv8.toString());
        assertEquals("0000000001000001", bv9.toString());
        assertEquals("0001", bv10.toString());
    }

    @Test
    void set() {
        BitVector bv = BitVector.parse("01101011", 8, 2);
        bv.set(0);
        assertEquals("01101011", bv.toString());
        bv.set(2);
        assertEquals("01101111", bv.toString());
        bv.set(7);
        assertEquals("11101111", bv.toString());
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> bv.set(8));
    }

    @Test
    void clear() {
        BitVector bv = BitVector.parse("01101011", 8, 2);
        bv.clear(0);
        assertEquals("01101010", bv.toString());
        bv.clear(1);
        assertEquals("01101000", bv.toString());
        bv.clear();
        assertEquals("00000000", bv.toString());
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> bv.clear(8));
    }

    @Test
    void get() {
        BitVector bv = BitVector.parse("01101011", 8, 2);
        assertTrue(bv.get(0));
        assertFalse(bv.get(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> bv.get(8));
    }

    @Test
    void size() {
        BitVector bv = BitVector.parse("01101011", 8, 2);
        assertEquals(8, bv.size());
    }

    @Test
    void testEquals() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("01101011", 8, 2);
        BitVector bv3 = BitVector.parse("11010101", 8, 2);
        assertTrue(bv1.equals(bv2));
        assertFalse(bv1.equals(bv3));
    }

}