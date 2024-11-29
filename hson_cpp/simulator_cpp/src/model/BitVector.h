#pragma once
#include <boost/dynamic_bitset/dynamic_bitset.hpp>
#include <utility>

namespace hson {
    class BitVector {
    public:
        [[nodiscard]] virtual int width() const = 0;

        [[nodiscard]] virtual bool toBool() const = 0;

        [[nodiscard]] virtual signed long toSL() const = 0;

        [[nodiscard]] virtual unsigned long toUL() const = 0;

        [[nodiscard]] virtual bool get(int index) const = 0;

        [[nodiscard]] virtual bool msb() const = 0;

        [[nodiscard]] virtual bool lsb() const = 0;

        [[nodiscard]] virtual BitVector *copyOf() const = 0;

        virtual void set(int index, bool value) = 0;

        virtual void setRange(int from, int to, const BitVector *value) = 0;

        [[nodiscard]] virtual BitVector *CAST(int length) const = 0;

        [[nodiscard]] virtual BitVector *SELECT(int from, int to) const = 0;

        [[nodiscard]] virtual BitVector *CONCAT(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *AND(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *NAND(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *XOR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *XNOR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *OR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *NOR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *ASHR(unsigned long offset) const = 0;

        [[nodiscard]] virtual BitVector *LSHR(unsigned long offset) const = 0;

        [[nodiscard]] virtual BitVector *SHL(unsigned long offset) const = 0;

        [[nodiscard]] virtual BitVector *ADD(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *SUB(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *MUL(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *DIV(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *NOT() const = 0;

        [[nodiscard]] virtual BitVector *NEG() const = 0;

        virtual bool operator==(const BitVector &other) const = 0;

        virtual bool operator!=(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool SLT(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool SLE(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool ULT(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool ULE(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool SGT(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool SGE(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool UGT(const BitVector &other) const = 0;

        [[nodiscard]] virtual bool UGE(const BitVector &other) const = 0;

        virtual ~BitVector() = default;

        [[nodiscard]] virtual std::string print() const = 0;
    };

    class BinaryBitVector final : public BitVector {
        boost::dynamic_bitset<> data;

        static constexpr int LONG_SIZE = sizeof(unsigned long) * 8;

        [[nodiscard]] const BinaryBitVector *requiresValidOperand(const BitVector &other) const;

    public:
        explicit BinaryBitVector(boost::dynamic_bitset<> data): data(std::move(data)) {
        }

        [[nodiscard]] static BinaryBitVector *parse(const std::string &literal);

        [[nodiscard]] static BinaryBitVector *fromBool(bool value);

        [[nodiscard]] static BinaryBitVector *fromUL(unsigned long value);

        [[nodiscard]] static BinaryBitVector *fromUL(unsigned int width, unsigned long value);

        [[nodiscard]] BitVector * copyOf() const override;

        [[nodiscard]] int width() const override;

        [[nodiscard]] bool toBool() const override;

        [[nodiscard]] signed long toSL() const override;

        [[nodiscard]] unsigned long toUL() const override;

        [[nodiscard]] bool get(int index) const override;

        [[nodiscard]] bool msb() const override;

        [[nodiscard]] bool lsb() const override;

        void set(int index, bool value) override;

        void setRange(int from, int to, const BitVector *value) override;

        [[nodiscard]] BitVector *SELECT(int from, int to) const override;

        [[nodiscard]] BitVector *CONCAT(const BitVector &other) const override;

        [[nodiscard]] BitVector *CAST(int length) const override;

        [[nodiscard]] BitVector *AND(const BitVector &other) const override;

        [[nodiscard]] BitVector *XOR(const BitVector &other) const override;

        [[nodiscard]] BitVector *OR(const BitVector &other) const override;

        [[nodiscard]] BitVector *NAND(const BitVector &other) const override;

        [[nodiscard]] BitVector *XNOR(const BitVector &other) const override;

        [[nodiscard]] BitVector *NOR(const BitVector &other) const override;

        [[nodiscard]] BitVector *NOT() const override;

        [[nodiscard]] BitVector *ASHR(unsigned long offset) const override;

        [[nodiscard]] BitVector *LSHR(unsigned long offset) const override;

        [[nodiscard]] BitVector *SHL(unsigned long offset) const override;

        [[nodiscard]] BitVector *ADD(const BitVector &other) const override;

        [[nodiscard]] BitVector *SUB(const BitVector &other) const override;

        [[nodiscard]] BitVector *MUL(const BitVector &other) const override;

        [[nodiscard]] BitVector *DIV(const BitVector &other) const override;

        [[nodiscard]] BitVector *NEG() const override;

        bool operator==(const BitVector &other) const override;

        bool operator!=(const BitVector &other) const override;

        [[nodiscard]] bool SLT(const BitVector &other) const override;

        [[nodiscard]] bool SLE(const BitVector &other) const override;

        [[nodiscard]] bool ULT(const BitVector &other) const override;

        [[nodiscard]] bool ULE(const BitVector &other) const override;

        [[nodiscard]] bool SGT(const BitVector &other) const override;

        [[nodiscard]] bool SGE(const BitVector &other) const override;

        [[nodiscard]] bool UGT(const BitVector &other) const override;

        [[nodiscard]] bool UGE(const BitVector &other) const override;

        [[nodiscard]] std::string print() const override;
    };
} // hson
