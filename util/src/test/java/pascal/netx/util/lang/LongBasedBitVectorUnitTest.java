package pascal.netx.util.lang;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class LongBasedBitVectorUnitTest {

    @Test
    void testBigInteger() {
        BigInteger value = BigInteger.valueOf(12389);
        LongBasedBitVector bv1 = new LongBasedBitVector(16, value.longValue());
        LongBasedBitVector bv2 = new LongBasedBitVector(50, value.longValue());
        LongBasedBitVector bv3 = new LongBasedBitVector(4, value.longValue());
        
        assertEquals(value, bv1.asUnsignedBI());
        assertEquals(value, bv2.asUnsignedBI());
        assertEquals(value.and(BigInteger.valueOf(15)), bv3.asUnsignedBI());
    }

    @Test
    void testExt() {
        LongBasedBitVector bv1 = new LongBasedBitVector(1, 0b1);
        LongBasedBitVector bv2 = new LongBasedBitVector(1, 0b0);
        LongBasedBitVector bv3 = new LongBasedBitVector(7, 0b1101010);
        LongBasedBitVector bv4 = new LongBasedBitVector(7, 0b0010010);

        LongBasedBitVector bv5 = new LongBasedBitVector(8, 0b11111111);
        LongBasedBitVector bv6 = new LongBasedBitVector(8, 0b00000000);
        LongBasedBitVector bv7 = new LongBasedBitVector(18, 0b111111111111101010);
        LongBasedBitVector bv8 = new LongBasedBitVector(18, 0b000000000000010010);
        assertEquals(bv5, bv1.sext(8));
        assertEquals(bv6, bv2.sext(8));
        assertEquals(bv7, bv3.sext(18));
        assertEquals(bv8, bv4.sext(18));

        LongBasedBitVector bv9 = new LongBasedBitVector(8, 0b00000001);
        LongBasedBitVector bv10 = new LongBasedBitVector(8, 0b00000000);
        LongBasedBitVector bv11 = new LongBasedBitVector(18, 0b000000000001101010);
        LongBasedBitVector bv12 = new LongBasedBitVector(18, 0b000000000000010010);
        assertEquals(bv9, bv1.uext(8));
        assertEquals(bv10, bv2.uext(8));
        assertEquals(bv11, bv3.uext(18));
        assertEquals(bv12, bv4.uext(18));
    }

    @Test
    void testTruncate() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(3, 0b011);
        LongBasedBitVector bv3 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv4 = new LongBasedBitVector(11, 0b10000010111);
        assertEquals(bv2, bv1.truncate(3));
        assertEquals(bv4, bv3.truncate(11));
    }

    @Test
    void testParse() {
        LongBasedBitVector bv1 = new LongBasedBitVector(7, Long.parseLong("1101011", 2));
        LongBasedBitVector bv2 = new LongBasedBitVector(8, Long.parseLong("01101011", 2));
        LongBasedBitVector bv3 = new LongBasedBitVector(18, Long.parseLong("110101001101101011", 2));
        LongBasedBitVector bv4 = new LongBasedBitVector(8, Long.parseLong("333", 8));
        LongBasedBitVector bv5 = new LongBasedBitVector(9, Long.parseLong("333", 8));
        LongBasedBitVector bv6 = new LongBasedBitVector(8, 95);
        LongBasedBitVector bv7 = new LongBasedBitVector(20, 95);
        LongBasedBitVector bv8 = new LongBasedBitVector(16, Long.parseLong("af41", 16));
        LongBasedBitVector bv9 = new LongBasedBitVector(16, Long.parseLong("41", 16));
        LongBasedBitVector bv10 = new LongBasedBitVector(4, Long.parseLong("41", 16));
        
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
        LongBasedBitVector bv = new LongBasedBitVector(8, 0b01101011);
        assertEquals(8, bv.size());
    }

    @Test
    void testConcat() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00010101);
        LongBasedBitVector bv3 = new LongBasedBitVector(16, 0b0110101100010101);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(25, 0b0010101111000101110101100L);
        LongBasedBitVector bv6 = new LongBasedBitVector(50, 0b10100101111110100000101110010101111000101110101100L);
        assertEquals(bv3, bv1.concat(bv2));
        assertEquals(bv6, bv4.concat(bv5));
    }

    @Test
    void testEquals() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b11010101);
        assertEquals(bv1, bv2);
        assertNotEquals(bv1, bv3);
    }

    @Test
    void testNeg() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b10010101);
        LongBasedBitVector bv3 = new LongBasedBitVector(25, 0b1010010111111010000010000L);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b0101101000000101111110000L);
        assertEquals(bv2, bv1.neg());
        assertEquals(bv4, bv3.neg());
    }

    @Test
    void testNot() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b10010100);
        LongBasedBitVector bv3 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b0101101000000101111101000L);
        assertEquals(bv2, bv1.not());
        assertEquals(bv4, bv3.not());
    }

    @Test
    void testAdd() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00010101);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b10000000);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(25, 0b0010101111000101110101100L);
        LongBasedBitVector bv6 = new LongBasedBitVector(25, 0b1101000110111111111000011L);
        
        var a = new LongBasedBitVector(32, Long.parseLong("6B52DC46", 16));
        var b = new LongBasedBitVector(32, Long.parseLong("9491C51A", 16));
        var c = new LongBasedBitVector(32, Long.parseLong("54C3E406", 16));
        var d = new LongBasedBitVector(32, Long.parseLong("357F60B5", 16));
        var e = new LongBasedBitVector(32, Long.parseLong("8A27E61B", 16));
        
        assertEquals(e, a.add(b).add(c).add(d));
        assertEquals(bv3, bv1.add(bv2));
        assertEquals(bv6, bv4.add(bv5));
    }

    @Test
    void testSub() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00010101);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b01010110);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(25, 0b0010101111000101110101100L);
        LongBasedBitVector bv6 = new LongBasedBitVector(25, 0b0111101000110100001101011L);
        assertEquals(bv3, bv1.sub(bv2));
        assertEquals(bv6, bv4.sub(bv5));
    }

    @Test
    void testPow() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b00001010);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00000011);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b11101000);
        assertEquals(bv3, bv1.pow(bv2));
    }

    @Test
    void testAnd() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00010101);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b00000001);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(25, 0b0010101111000101110101100L);
        LongBasedBitVector bv6 = new LongBasedBitVector(25, 0b0010000111000000000000100L);
        assertEquals(bv3, bv1.and(bv2));
        assertEquals(bv6, bv4.and(bv5));
    }

    @Test
    void testAshr() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00000011);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b00001101);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(8, 0b00001010);
        LongBasedBitVector bv6 = new LongBasedBitVector(25, 0b1111111111101001011111101L);
        assertEquals(bv3, bv1.ashr(bv2.toUInt()));
        assertEquals(bv6, bv4.ashr(bv5.toUInt()));
    }

    @Test
    void testLshr() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00000011);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b00001101);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(8, 0b00001010);
        LongBasedBitVector bv6 = new LongBasedBitVector(25, 0b0000000000101001011111101L);
        assertEquals(bv3, bv1.lshr(bv2.toUInt()));
        assertEquals(bv6, bv4.lshr(bv5.toUInt()));
    }

    @Test
    void testShl() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00000011);
        LongBasedBitVector bv3 = new LongBasedBitVector(8, 0b01011000);
        LongBasedBitVector bv4 = new LongBasedBitVector(25, 0b1010010111111010000010111L);
        LongBasedBitVector bv5 = new LongBasedBitVector(8, 0b00001010);
        LongBasedBitVector bv6 = new LongBasedBitVector(25, 0b1110100000101110000000000L);
        assertEquals(bv3, bv1.shl(bv2.toUInt()));
        assertEquals(bv6, bv4.shl(bv5.toUInt()));
    }

    @Test
    void testGet() {
        var literal = "110101001101101011";
        LongBasedBitVector bv = new LongBasedBitVector(18, Long.parseLong(literal, 2));
        for (int i = 0; i < literal.length(); i++) {
            assertEquals(literal.charAt(18 - i - 1) == '1', bv.get(i));
        }
    }

    @Test
    void testSelect() {
        LongBasedBitVector bv = new LongBasedBitVector(18, Long.parseLong("110101001101101011", 2));
        assertEquals(new LongBasedBitVector(9, Long.parseLong("101101011", 2)), bv.select(8, 0));
        assertEquals(new LongBasedBitVector(9, Long.parseLong("110101001", 2)), bv.select(17, 9));
        assertEquals(new LongBasedBitVector(18, Long.parseLong("110101001101101011", 2)), bv.select(17, 0));
    }

    @Test
    void testRelational() {
        LongBasedBitVector zero = new LongBasedBitVector(32, 0);
        LongBasedBitVector one = new LongBasedBitVector(32, 1145141);
        LongBasedBitVector neg = zero.sub(one);

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

    @Test
    void testOr() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00010101);
        LongBasedBitVector result = new LongBasedBitVector(8, 0b01111111);
        assertEquals(result, bv1.or(bv2));
    }

    @Test
    void testXor() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b01101011);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b00010101);
        LongBasedBitVector result = new LongBasedBitVector(8, 0b01111110);
        assertEquals(result, bv1.xor(bv2));
    }

    @Test
    void testIsZero() {
        LongBasedBitVector zero = new LongBasedBitVector(32, 0);
        LongBasedBitVector nonZero = new LongBasedBitVector(32, 123);
        assertTrue(zero.isZero());
        assertFalse(nonZero.isZero());
    }

    @Test
    void testMsbLsb() {
        LongBasedBitVector bv1 = new LongBasedBitVector(8, 0b10101010);
        LongBasedBitVector bv2 = new LongBasedBitVector(8, 0b01010101);
        assertTrue(bv1.msb());
        assertFalse(bv2.msb());
        assertFalse(bv1.lsb());
        assertTrue(bv2.lsb());
    }

    @Test
    void test64BitOperations() {
        LongBasedBitVector bv1 = new LongBasedBitVector(64, 0xFFFFFFFFFFFFFFFFL);
        LongBasedBitVector bv2 = new LongBasedBitVector(64, 1L);
        LongBasedBitVector sum = new LongBasedBitVector(64, 0L);
        assertEquals(sum, bv1.add(bv2));
    }
}

