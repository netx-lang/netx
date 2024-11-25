#pragma once
#include <bitset>
#include <memory>
#include <stdexcept>

#include <boost/dynamic_bitset/dynamic_bitset.hpp>
#include <utility>

namespace hson{
    class BitVector {
    public:
        [[nodiscard]] virtual int size() const = 0;

        [[nodiscard]] virtual unsigned long toUL() const = 0;

        [[nodiscard]] virtual bool get(int index) const = 0;

        virtual void set(int index, bool value) = 0;

        [[nodiscard]] virtual BitVector *AND(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *XOR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *OR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *ASHR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *LSHR(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *SHL(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *ADD(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *SUB(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *MUL(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *DIV(const BitVector &other) const = 0;

        [[nodiscard]] virtual BitVector *NOT() const = 0;

        [[nodiscard]] virtual BitVector *NEG() const = 0;

        virtual ~BitVector() = default;
    };

    class BinaryBitVector final : public BitVector {
        boost::dynamic_bitset<> data;

        static constexpr int LONG_SIZE = sizeof(unsigned long) * 8;

        explicit BinaryBitVector(boost::dynamic_bitset<> data): data(std::move(data)) {}

        [[nodiscard]] const BinaryBitVector *requiresValidOperand(const BitVector &other) const {
            if (const auto* that = dynamic_cast<const BinaryBitVector*>(&other); that != nullptr) {
                if (that->size() == this->size()) {
                    return that;
                }
            }
            throw std::runtime_error("Invalid operand");
        }

    public:
        [[nodiscard]] static BinaryBitVector *parse(const std::string &literal) {
            static std::unordered_map<char, int8_t> hexMap = {
                {'0', 0}, {'1', 1}, {'2', 2}, {'3', 3},
                {'4', 4}, {'5', 5}, {'6', 6}, {'7', 7},
                {'8', 8}, {'9', 9}, {'A', 10}, {'B', 11},
                {'C', 12}, {'D', 13}, {'E', 14}, {'F', 15}
            };

            if (const auto pos = literal.find('\''); pos != std::string::npos) {
                const long size = atol(literal.substr(0, pos).c_str());
                auto result = boost::dynamic_bitset<>(size);
                int index = 0;
                for (auto i = literal.size() - 1; i >= pos + 2; --i) {
                    auto hex = hexMap[literal[i]];
                    for (int j = 0; j != 4; ++j) {
                        result.set(index++, hex & 1);
                        hex >>= 1;
                    }
                }
                return new BinaryBitVector(result);

            }
            return new BinaryBitVector(
                boost::dynamic_bitset(32, atol(literal.c_str())));
        }

        [[nodiscard]] int size() const override {
            return static_cast<int>(data.size());
        }

        [[nodiscard]] unsigned long toUL() const override {
            return data.to_ulong();
        }

        [[nodiscard]] bool get(int index) const override {
            return data.test(index);
        }

        void set(int index, const bool value) override {
            data.set(index, value);
        }

        [[nodiscard]] BitVector *AND(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            return new BinaryBitVector(this->data & that->data);
        }

        [[nodiscard]] BitVector *XOR(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            return new BinaryBitVector(this->data ^ that->data);
        }

        [[nodiscard]] BitVector *OR(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            return new BinaryBitVector(this->data | that->data);
        }

        [[nodiscard]] BitVector *NOT() const override {
            return new BinaryBitVector(~data);
        }

        [[nodiscard]] BitVector *ASHR(const BitVector &other) const override {
            const auto offset = other.toUL();
            auto result = data >> offset;
            if (data.test(data.size() - 1)) {
                for (int i = 0; i != offset; ++i) {
                    result.set(data.size() - 1 - i);
                }
            }
            return new BinaryBitVector(result);
        }

        [[nodiscard]] BitVector *LSHR(const BitVector &other) const override {
            const auto offset = other.toUL();
            return new BinaryBitVector(data >> offset);
        }

        [[nodiscard]] BitVector *SHL(const BitVector &other) const override {
            const auto offset = other.toUL();
            return new BinaryBitVector(data << offset);
        }

        [[nodiscard]] BitVector *ADD(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            if (size() <= LONG_SIZE) {
                return new BinaryBitVector(
                    boost::dynamic_bitset(size(), this->toUL() + that->toUL()));
            }
            bool carry = false;
            boost::dynamic_bitset<> result(size());
            for (int i = 0; i != size(); ++i) {
                const bool sum = this->get(i) ^ that->get(i);
                result.set(i, sum ^ carry);
                carry = this->get(i) & that->get(i) | carry & sum;
            }
            return new BinaryBitVector(result);
        }

        [[nodiscard]] BitVector *SUB(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            if (size() <= LONG_SIZE) {
                return new BinaryBitVector(
                    boost::dynamic_bitset(size(), this->toUL() - that->toUL()));
            }
            const auto neg = that->NEG();
            const auto result = this->ADD(*neg);
            delete neg;
            return result;
        }

        [[nodiscard]] BitVector *MUL(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            if (size() <= LONG_SIZE) {
                return new BinaryBitVector(
                        boost::dynamic_bitset(size(), this->toUL() * that->toUL()));
            }
            throw std::runtime_error("TODO: Not implemented");
        }

        [[nodiscard]] BitVector *DIV(const BitVector &other) const override {
            const auto that = requiresValidOperand(other);
            if (size() <= LONG_SIZE) {
                return new BinaryBitVector(
                        boost::dynamic_bitset(size(), this->toUL() / that->toUL()));
            }
            throw std::runtime_error("TODO: Not implemented");
        }

        [[nodiscard]] BitVector *NEG() const override {
            boost::dynamic_bitset<> result = ~data;
            for (int i = 0; i != size(); ++i) {
                if (result[i]) {
                    result.reset(i);
                } else {
                    result.set(i);
                    break;
                }
            }
            return new BinaryBitVector(result);
        }
    };

} // hson
