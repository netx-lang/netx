package pascal.netx.util.lang;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class BitVectorUnitTest {

    @Test
    void testBigInteger() {
        BigInteger value = BigInteger.valueOf(12389);
        assertEquals(value, BitVector.fromBigInteger(16, value).toBigInteger());
        assertEquals(value, BitVector.fromBigInteger(50, value).toBigInteger());
        assertEquals(value.and(BigInteger.valueOf(15)), BitVector.fromBigInteger(4, value).toBigInteger());
    }

    @Test
    void testExt() {
        BitVector bv1 = BitVector.parse("1", 1, 2);
        BitVector bv2 = BitVector.parse("0", 1, 2);
        BitVector bv3 = BitVector.parse("1101010", 7, 2);
        BitVector bv4 = BitVector.parse("0010010", 7, 2);

        BitVector bv5 = BitVector.parse("11111111", 8, 2);
        BitVector bv6 = BitVector.parse("00000000", 8, 2);
        BitVector bv7 = BitVector.parse("111111111111101010", 18, 2);
        BitVector bv8 = BitVector.parse("000000000000010010", 18, 2);
        assertEquals(bv5, bv1.sext(8));
        assertEquals(bv6, bv2.sext(8));
        assertEquals(bv7, bv3.sext(18));
        assertEquals(bv8, bv4.sext(18));

        BitVector bv9 = BitVector.parse("0000001", 8, 2);
        BitVector bv10 = BitVector.parse("0000000", 8, 2);
        BitVector bv11 = BitVector.parse("000000000001101010", 18, 2);
        BitVector bv12 = BitVector.parse("000000000000010010", 18, 2);
        assertEquals(bv9, bv1.uext(8));
        assertEquals(bv10, bv2.uext(8));
        assertEquals(bv11, bv3.uext(18));
        assertEquals(bv12, bv4.uext(18));
    }

    @Test
    void testTruncate() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("011", 3, 2);
        BitVector bv3 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv4 = BitVector.parse("10000010111", 11, 2);
        assertEquals(bv2, bv1.truncate(3));
        assertEquals(bv4, bv3.truncate(11));
    }

    @Test
    void testParse() {
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
        assertEquals("7'h6B", bv1.toString());
        assertEquals("8'h6B", bv2.toString());
        assertEquals("18'h3536B", bv3.toString());
        assertEquals("8'hDB", bv4.toString());
        assertEquals("9'hDB", bv5.toString());
        assertEquals("8'h5F", bv6.toString());
        assertEquals("20'h5F", bv7.toString());
        assertEquals("16'hAF41", bv8.toString());
        assertEquals("16'h41", bv9.toString());
        assertEquals("4'h1", bv10.toString());
    }

    @Test
    void testSize() {
        BitVector bv = BitVector.parse("01101011", 8, 2);
        assertEquals(8, bv.size());
    }

    @Test
    void testConcat() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00010101", 8, 2);
        BitVector bv3 = BitVector.parse("0110101100010101", 16, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("0010101111000101110101100", 25, 2);
        BitVector bv6 = BitVector.parse("10100101111110100000101110010101111000101110101100", 50, 2);
        assertEquals(bv3, bv1.concat(bv2));
        assertEquals(bv6, bv4.concat(bv5));
    }

    @Test
    void testEquals() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("01101011", 8, 2);
        BitVector bv3 = BitVector.parse("11010101", 8, 2);
        assertEquals(bv1, bv2);
        assertNotEquals(bv1, bv3);
    }

    @Test
    void testNeg() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("10010101", 8, 2);
        BitVector bv3 = BitVector.parse("1010010111111010000010000", 25, 2);
        BitVector bv4 = BitVector.parse("0101101000000101111110000", 25, 2);
        assertEquals(bv2, bv1.neg());
        assertEquals(bv4, bv3.neg());
    }

    @Test
    void testNot() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("10010100", 8, 2);
        BitVector bv3 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv4 = BitVector.parse("0101101000000101111101000", 25, 2);
        assertEquals(bv2, bv1.not());
        assertEquals(bv4, bv3.not());
    }

    @Test
    void testAdd() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00010101", 8, 2);
        BitVector bv3 = BitVector.parse("10000000", 8, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("0010101111000101110101100", 25, 2);
        BitVector bv6 = BitVector.parse("1101000110111111111000011", 25, 2);
        var a = BitVector.parse("6B52DC46", 32, 16);
        var b = BitVector.parse("9491C51A", 32, 16);
        var c = BitVector.parse("54C3E406", 32, 16);
        var d = BitVector.parse("357F60B5", 32, 16);
        var e = BitVector.parse("8A27E61B", 32, 16);
        assertEquals(e, a.add(b).add(c).add(d));
        assertEquals(bv3, bv1.add(bv2));
        assertEquals(bv6, bv4.add(bv5));
    }

    @Test
    void testSub() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00010101", 8, 2);
        BitVector bv3 = BitVector.parse("01010110", 8, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("0010101111000101110101100", 25, 2);
        BitVector bv6 = BitVector.parse("0111101000110100001101011", 25, 2);
        assertEquals(bv3, bv1.sub(bv2));
        assertEquals(bv6, bv4.sub(bv5));
    }

    @Test
    void testPow() {
        BitVector bv1 = BitVector.parse("00001010", 8, 2);
        BitVector bv2 = BitVector.parse("00000011", 8, 2);
        BitVector bv3 = BitVector.parse("11101000", 8, 2);
        assertEquals(bv3, bv1.pow(bv2));
    }

    @Test
    void testAnd() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00010101", 8, 2);
        BitVector bv3 = BitVector.parse("00000001", 8, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("0010101111000101110101100", 25, 2);
        BitVector bv6 = BitVector.parse("0010000111000000000000100", 25, 2);
        assertEquals(bv3, bv1.and(bv2));
        assertEquals(bv6, bv4.and(bv5));
    }

    @Test
    void testAshr() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00000011", 8, 2);
        BitVector bv3 = BitVector.parse("00001101", 8, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("00001010", 8, 2);
        BitVector bv6 = BitVector.parse("1111111111101001011111101", 25, 2);
        assertEquals(bv3, bv1.ashr(bv2.toInt()));
        assertEquals(bv6, bv4.ashr(bv5.toInt()));
    }

    @Test
    void testLshr() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00000011", 8, 2);
        BitVector bv3 = BitVector.parse("00001101", 8, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("00001010", 8, 2);
        BitVector bv6 = BitVector.parse("0000000000101001011111101", 25, 2);
        assertEquals(bv3, bv1.lshr(bv2.toInt()));
        assertEquals(bv6, bv4.lshr(bv5.toInt()));
    }

    @Test
    void testShl() {
        BitVector bv1 = BitVector.parse("01101011", 8, 2);
        BitVector bv2 = BitVector.parse("00000011", 8, 2);
        BitVector bv3 = BitVector.parse("01011000", 8, 2);
        BitVector bv4 = BitVector.parse("1010010111111010000010111", 25, 2);
        BitVector bv5 = BitVector.parse("00001010", 8, 2);
        BitVector bv6 = BitVector.parse("1110100000101110000000000", 25, 2);
        assertEquals(bv3, bv1.shl(bv2.toInt()));
        assertEquals(bv6, bv4.shl(bv5.toInt()));
    }

    @Test
    void testGet() {
        var literal = "110101001101101011";
        BitVector bv = BitVector.parse(literal, 18, 2);
        for (int i = 0; i < literal.length(); i++) {
            assertEquals(literal.charAt(18 - i - 1) == '1', bv.get(i));
        }
    }

    @Test
    void testSelect() {
        BitVector bv = BitVector.parse("110101001101101011", 18, 2);
        assertEquals(BitVector.parse("101101011", 9, 2), bv.select(8, 0));
        assertEquals(BitVector.parse("110101001", 9, 2), bv.select(17, 9));
        assertEquals(BitVector.parse("110101001101101011", 18, 2), bv.select(17, 0));
    }

    @Test
    void testRelational() {
        BitVector zero = BitVector.parse("0", 32, 10);
        BitVector one = BitVector.parse("1145141", 32, 10);
        BitVector neg = zero.sub(one);

        assertTrue(neg.slt(one));
        assertTrue(neg.sle(one));
        assertFalse(neg.sgt(one));
        assertFalse(neg.sge(one));
        assertFalse(neg.eq(one));
        assertTrue(neg.ne(one));
        assertFalse(neg.ult(one));
        assertFalse(neg.ule(one));
        assertTrue(neg.ugt(one));
        assertTrue(neg.uge(one));

        assertTrue(neg.slt(zero));
        assertTrue(neg.sle(zero));
        assertFalse(neg.sgt(zero));
        assertFalse(neg.sge(zero));
        assertFalse(neg.eq(zero));
        assertTrue(neg.ne(zero));
        assertFalse(neg.ult(zero));
        assertFalse(neg.ule(zero));
        assertTrue(neg.ugt(zero));
        assertTrue(neg.uge(zero));

        assertTrue(zero.slt(one));
        assertTrue(zero.sle(one));
        assertFalse(zero.sgt(one));
        assertFalse(zero.sge(one));
        assertFalse(zero.eq(one));
        assertTrue(zero.ne(one));
        assertTrue(zero.ult(one));
        assertTrue(zero.ule(one));
        assertFalse(zero.ugt(one));
        assertFalse(zero.uge(one));
    }
}