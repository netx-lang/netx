package pascal.netx.util.lang;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests specifically for ArrayBasedBitVector implementation.
 * Tests BitVectors with size > 64 bits and edge cases for byte array implementation.
 */
class ArrayBasedBitVectorUnitTest {

    @Test
    void testBigInteger() {
        BigInteger value = BigInteger.valueOf(12389);
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(16, value);
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(128, value);
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(4, value);
        
        assertEquals(value, bv1.asUnsignedBI());
        assertEquals(value, bv2.asUnsignedBI());
        assertEquals(value.and(BigInteger.valueOf(15)), bv3.asUnsignedBI());

        BigInteger negValue = BigInteger.valueOf(-12389);
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(16, negValue);
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(128, negValue);
        assertEquals(negValue, bv4.asSignedBI());
        assertEquals(negValue, bv5.asSignedBI());

        BigInteger negValue2 = BigInteger.valueOf(-614231231289L);
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(128, negValue2);
        assertEquals(negValue2, bv6.asSignedBI());
    }

    @Test
    void testLargeBigInteger() {
        // Test with values larger than 64 bits
        BigInteger largeValue = new BigInteger("123456789012345678901234567890");
        ArrayBasedBitVector bv = new ArrayBasedBitVector(128, largeValue);
        assertEquals(largeValue, bv.asUnsignedBI());
    }

    @Test
    void testExt() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(1, BigInteger.valueOf(0b1));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(1, BigInteger.valueOf(0b0));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(7, BigInteger.valueOf(0b1101010));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(7, BigInteger.valueOf(0b0010010));

        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b11111111));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000000));
        ArrayBasedBitVector bv7 = new ArrayBasedBitVector(18, BigInteger.valueOf(0b111111111111101010));
        ArrayBasedBitVector bv8 = new ArrayBasedBitVector(18, BigInteger.valueOf(0b000000000000010010));
        assertEquals(bv5, bv1.sext(8));
        assertEquals(bv6, bv2.sext(8));
        assertEquals(bv7, bv3.sext(18));
        assertEquals(bv8, bv4.sext(18));

        ArrayBasedBitVector bv9 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000001));
        ArrayBasedBitVector bv10 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000000));
        ArrayBasedBitVector bv11 = new ArrayBasedBitVector(18, BigInteger.valueOf(0b000000000001101010));
        ArrayBasedBitVector bv12 = new ArrayBasedBitVector(18, BigInteger.valueOf(0b000000000000010010));
        assertEquals(bv9, bv1.uext(8));
        assertEquals(bv10, bv2.uext(8));
        assertEquals(bv11, bv3.uext(18));
        assertEquals(bv12, bv4.uext(18));
    }

    @Test
    void testLargeExt() {
        // Test extension with large bit vectors
        ArrayBasedBitVector bv = new ArrayBasedBitVector(65, BigInteger.ONE.shiftLeft(64));
        ArrayBasedBitVector sext = bv.sext(128);
        assertEquals(128, sext.size());
        assertTrue(sext.msb());
    }

    @Test
    void testTruncate() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(3, BigInteger.valueOf(0b011));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(11, BigInteger.valueOf(0b10000010111));
        assertEquals(bv2, bv1.truncate(3));
        assertEquals(bv4, bv3.truncate(11));
    }

    @Test
    void testLargeTruncate() {
        // Test truncation with large bit vectors
        BigInteger largeValue = new BigInteger("123456789012345678901234567890");
        ArrayBasedBitVector bv = new ArrayBasedBitVector(256, largeValue);
        ArrayBasedBitVector truncated = bv.truncate(64);
        assertEquals(64, truncated.size());
    }

    @Test
    void testParse() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(7, new BigInteger("1101011", 2));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, new BigInteger("01101011", 2));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(18, new BigInteger("110101001101101011", 2));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(8, new BigInteger("333", 8));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(9, new BigInteger("333", 8));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(8, BigInteger.valueOf(95));
        ArrayBasedBitVector bv7 = new ArrayBasedBitVector(20, BigInteger.valueOf(95));
        ArrayBasedBitVector bv8 = new ArrayBasedBitVector(16, new BigInteger("af41", 16));
        ArrayBasedBitVector bv9 = new ArrayBasedBitVector(16, new BigInteger("41", 16));
        ArrayBasedBitVector bv10 = new ArrayBasedBitVector(4, new BigInteger("41", 16));
        
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
        ArrayBasedBitVector bv = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        assertEquals(8, bv.size());
        
        ArrayBasedBitVector largeBv = new ArrayBasedBitVector(256, BigInteger.ONE);
        assertEquals(256, largeBv.size());
    }

    @Test
    void testConcat() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00010101));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(16, BigInteger.valueOf(0b0110101100010101));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0010101111000101110101100L));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(50, new BigInteger("10100101111110100000101110010101111000101110101100", 2));
        assertEquals(bv3, bv1.concat(bv2));
        assertEquals(bv6, bv4.concat(bv5));
    }

    @Test
    void testLargeConcat() {
        // Test concatenation with large bit vectors
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(128, BigInteger.valueOf(0xDEADBEEFL));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(128, BigInteger.valueOf(0xCAFEBABEL));
        BitVector result = bv1.concat(bv2);
        assertEquals(256, result.size());
    }

    @Test
    void testEquals() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b11010101));
        assertEquals(bv1, bv2);
        assertNotEquals(bv1, bv3);
    }

    @Test
    void testNeg() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b10010101));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010000L));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0101101000000101111110000L));
        assertEquals(bv2, bv1.neg());
        assertEquals(bv4, bv3.neg());
    }

    @Test
    void testNot() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b10010100));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0101101000000101111101000L));
        assertEquals(bv2, bv1.not());
        assertEquals(bv4, bv3.not());
    }

    @Test
    void testAdd() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00010101));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b10000000));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0010101111000101110101100L));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1101000110111111111000011L));
        
        var a = new ArrayBasedBitVector(32, new BigInteger("6B52DC46", 16));
        var b = new ArrayBasedBitVector(32, new BigInteger("9491C51A", 16));
        var c = new ArrayBasedBitVector(32, new BigInteger("54C3E406", 16));
        var d = new ArrayBasedBitVector(32, new BigInteger("357F60B5", 16));
        var e = new ArrayBasedBitVector(32, new BigInteger("8A27E61B", 16));
        
        assertEquals(e, a.add(b).add(c).add(d));
        assertEquals(bv3, bv1.add(bv2));
        assertEquals(bv6, bv4.add(bv5));
    }

    @Test
    void testLargeAdd() {
        // Test addition with large bit vectors
        BigInteger val1 = new BigInteger("123456789012345678901234567890");
        BigInteger val2 = new BigInteger("987654321098765432109876543210");
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(256, val1);
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(256, val2);
        BitVector result = bv1.add(bv2);
        assertEquals(val1.add(val2).and(BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE)), result.asUnsignedBI());
    }

    @Test
    void testSub() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00010101));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01010110));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0010101111000101110101100L));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0111101000110100001101011L));
        assertEquals(bv3, bv1.sub(bv2));
        assertEquals(bv6, bv4.sub(bv5));
    }

    @Test
    void testPow() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00001010));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000011));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b11101000));
        assertEquals(bv3, bv1.pow(bv2));
    }

    @Test
    void testAnd() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00010101));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000001));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0010101111000101110101100L));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0010000111000000000000100L));
        assertEquals(bv3, bv1.and(bv2));
        assertEquals(bv6, bv4.and(bv5));
    }

    @Test
    void testAshr() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000011));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00001101));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00001010));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1111111111101001011111101L));
        assertEquals(bv3, bv1.ashr(bv2.toUInt()));
        assertEquals(bv6, bv4.ashr(bv5.toUInt()));
    }

    @Test
    void testLshr() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000011));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00001101));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00001010));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b0000000000101001011111101L));
        assertEquals(bv3, bv1.lshr(bv2.toUInt()));
        assertEquals(bv6, bv4.lshr(bv5.toUInt()));
    }

    @Test
    void testShl() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00000011));
        ArrayBasedBitVector bv3 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01011000));
        ArrayBasedBitVector bv4 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1010010111111010000010111L));
        ArrayBasedBitVector bv5 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00001010));
        ArrayBasedBitVector bv6 = new ArrayBasedBitVector(25, BigInteger.valueOf(0b1110100000101110000000000L));
        assertEquals(bv3, bv1.shl(bv2.toUInt()));
        assertEquals(bv6, bv4.shl(bv5.toUInt()));
    }

    @Test
    void testGet() {
        var literal = "110101001101101011";
        ArrayBasedBitVector bv = new ArrayBasedBitVector(18, new BigInteger(literal, 2));
        for (int i = 0; i < literal.length(); i++) {
            assertEquals(literal.charAt(18 - i - 1) == '1', bv.get(i));
        }
    }

    @Test
    void testSelect() {
        ArrayBasedBitVector value = new ArrayBasedBitVector(18, new BigInteger("110101001101101011", 2));
        var bv1 = new ArrayBasedBitVector(9, new BigInteger("101101011", 2));
        var bv2 = new ArrayBasedBitVector(9, new BigInteger("110101001", 2));
        var bv3 = new ArrayBasedBitVector(18, new BigInteger("110101001101101011", 2));
        assertTrue(bv1.eq(value.select(8, 0)));
        assertTrue(bv2.eq(value.select(17, 9)));
        assertTrue(bv3.eq(value.select(17, 0)));
    }

    @Test
    void testLargeSelect() {
        // Test select with large bit vectors
        ArrayBasedBitVector bv = new ArrayBasedBitVector(256, new BigInteger("123456789012345678901234567890"));
        BitVector selected = bv.select(127, 0);
        assertEquals(128, selected.size());
    }

    @Test
    void testRelational() {
        ArrayBasedBitVector zero = new ArrayBasedBitVector(32, BigInteger.valueOf(0));
        ArrayBasedBitVector one = new ArrayBasedBitVector(32, BigInteger.valueOf(1145141));
        ArrayBasedBitVector neg = zero.sub(one);

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
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00010101));
        ArrayBasedBitVector result = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01111111));
        assertEquals(result, bv1.or(bv2));
    }

    @Test
    void testXor() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01101011));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b00010101));
        ArrayBasedBitVector result = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01111110));
        assertEquals(result, bv1.xor(bv2));
    }

    @Test
    void testIsZero() {
        ArrayBasedBitVector zero = new ArrayBasedBitVector(128, BigInteger.ZERO);
        ArrayBasedBitVector nonZero = new ArrayBasedBitVector(128, BigInteger.valueOf(123));
        assertTrue(zero.isZero());
        assertFalse(nonZero.isZero());
    }

    @Test
    void testMsbLsb() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b10101010));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(8, BigInteger.valueOf(0b01010101));
        assertTrue(bv1.msb());
        assertFalse(bv2.msb());
        assertFalse(bv1.lsb());
        assertTrue(bv2.lsb());
    }

    @Test
    void testLargeBitVector() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(64, new BigInteger("FFFFFFFFFFFFFFFF", 16));
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(128, BigInteger.ONE);
        ArrayBasedBitVector sum = bv1.concat(bv1).add(bv2);
        assertTrue(sum.isZero());
    }

    @Test
    void testCrossByteBoundary() {
        // Test operations that cross byte boundaries
        ArrayBasedBitVector bv = new ArrayBasedBitVector(12, BigInteger.valueOf(0xFFF));
        assertEquals(12, bv.size());
        assertTrue(bv.get(0));
        assertTrue(bv.get(7));
        assertTrue(bv.get(8));
        assertTrue(bv.get(11));
    }

    @Test
    void testToString() {
        ArrayBasedBitVector bv1 = new ArrayBasedBitVector(128, BigInteger.ZERO);
        assertTrue(bv1.toString().startsWith("128'h"));
        
        ArrayBasedBitVector bv2 = new ArrayBasedBitVector(100, new BigInteger("DEADBEEFCAFEBABE", 16));
        assertTrue(bv2.toString().contains("h"));
    }
}

