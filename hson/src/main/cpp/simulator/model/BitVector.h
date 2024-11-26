#pragma once
#include <bitset>
#include <memory>
#include <stdexcept>

#include <boost/dynamic_bitset/dynamic_bitset.hpp>
#include <utility>

namespace hson {
    class BitVector {
    public:
        [[nodiscard]] virtual int size() const = 0;

        [[nodiscard]] virtual unsigned long toUL() const = 0;

        [[nodiscard]] virtual bool get(int index) const = 0;

        [[nodiscard]] virtual bool msb() const = 0;

        [[nodiscard]] virtual bool lsb() const = 0;

        virtual void set(int index, bool value) = 0;

        virtual void setRange(int from, int to, BitVector *value) = 0;

        [[nodiscard]] virtual BitVector *CAST(int length) = 0;

        [[nodiscard]] virtual BitVector *SELECT(int from, int to) = 0;

        [[nodiscard]] virtual BitVector *AND(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *XOR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *OR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *ASHR(int offset) const = 0;

        [[nodiscard]] virtual BitVector *LSHR(int offset) const = 0;

        [[nodiscard]] virtual BitVector *SHL(int offset) const = 0;

        [[nodiscard]] virtual BitVector *ADD(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *SUB(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *MUL(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *DIV(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *NOT() const = 0;

        [[nodiscard]] virtual BitVector *NEG() const = 0;

        virtual bool operator==(const BitVector &other) const = 0;

        virtual ~BitVector() = default;
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

        [[nodiscard]] int size() const override;

        [[nodiscard]] unsigned long toUL() const override;

        [[nodiscard]] bool get(int index) const override;

        [[nodiscard]] bool msb() const override;

        [[nodiscard]] bool lsb() const override;

        void set(int index, bool value) override;

        void setRange(int from, int to, BitVector *value) override;

        [[nodiscard]] BitVector *SELECT(int from, int to) override;

        [[nodiscard]] BitVector *CAST(int length) override;

        [[nodiscard]] BitVector *AND(const BitVector &other) const override;

        [[nodiscard]] BitVector *XOR(const BitVector &other) const override;

        [[nodiscard]] BitVector *OR(const BitVector &other) const override;

        [[nodiscard]] BitVector *NOT() const override;

        [[nodiscard]] BitVector *ASHR(int offset) const override;

        [[nodiscard]] BitVector *LSHR(int offset) const override;

        [[nodiscard]] BitVector *SHL(int offset) const override;

        [[nodiscard]] BitVector *ADD(const BitVector &other) const override;

        [[nodiscard]] BitVector *SUB(const BitVector &other) const override;

        [[nodiscard]] BitVector *MUL(const BitVector &other) const override;

        [[nodiscard]] BitVector *DIV(const BitVector &other) const override;

        [[nodiscard]] BitVector *NEG() const override;

        bool operator==(const BitVector &other) const override;
    };
} // hson
