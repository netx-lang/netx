#include "BitVector.h"

namespace hson {
    const BinaryBitVector *BinaryBitVector::requiresValidOperand(const BitVector &other) const {
        if (const auto *that = dynamic_cast<const BinaryBitVector *>(&other); that != nullptr) {
            if (that->size() == this->size()) {
                return that;
            }
        }
        throw std::runtime_error("Invalid operand");
    }

    BinaryBitVector *BinaryBitVector::parse(const std::string &literal) {
        static std::unordered_map<char, int8_t> digits = {
            {'0', 0}, {'1', 1}, {'2', 2}, {'3', 3},
            {'4', 4}, {'5', 5}, {'6', 6}, {'7', 7},
            {'8', 8}, {'9', 9}, {'A', 10}, {'B', 11},
            {'C', 12}, {'D', 13}, {'E', 14}, {'F', 15}
        };

        if (const auto pos = literal.find('\''); pos != std::string::npos) {
            const long size = std::stoi(literal.substr(0, pos));
            int index = 0, bit;
            switch (literal[pos + 1]) {
                case 'd': {
                    return new BinaryBitVector(
                        boost::dynamic_bitset(size, std::stoi(literal.substr(pos + 2))));
                }
                case 'b': bit = 1;
                    break;
                case 'o': bit = 3;
                    break;
                case 'h': bit = 4;
                    break;
                default: throw std::runtime_error("Invalid literal");
            }
            auto result = boost::dynamic_bitset<>(size);
            for (auto i = literal.size() - 1; i >= pos + 2; --i) {
                auto value = digits[literal[i]];
                for (int j = 0; j != bit; ++j) {
                    result.set(index++, value & 1);
                    value >>= 1;
                }
            }
            return new BinaryBitVector(result);
        }
        return new BinaryBitVector(
            boost::dynamic_bitset(32, std::stoi(literal)));
    }

    BinaryBitVector *BinaryBitVector::fromBool(const bool value) {
        return new BinaryBitVector(boost::dynamic_bitset(1, value));
    }

    BinaryBitVector *BinaryBitVector::fromUL(const unsigned long value) {
        return new BinaryBitVector(boost::dynamic_bitset(32, value));
    }


    int BinaryBitVector::size() const {
        return static_cast<int>(data.size());
    }

    unsigned long BinaryBitVector::toUL() const {
        return data.to_ulong();
    }

    bool BinaryBitVector::get(const int index) const {
        return data.test(index);
    }

    bool BinaryBitVector::msb() const {
        return get(size() - 1);
    }

    bool BinaryBitVector::lsb() const {
        return get(0);
    }

    void BinaryBitVector::set(const int index, const bool value) {
        data.set(index, value);
    }

    void BinaryBitVector::setRange(const int from, const int to, BitVector *value) {
        const int msb = std::max(from, to), lsb = std::min(from, to);
        if (value->size() != msb - lsb + 1) {
            throw std::runtime_error("Invalid value");
        }
        for (int i = lsb; i <= msb; ++i) {
            set(i, value->get(i - lsb));
        }
    }

    BitVector *BinaryBitVector::SELECT(const int from, const int to) {
        const int msb = std::max(from, to), lsb = std::min(from, to);
        auto result = boost::dynamic_bitset(data >> lsb);
        result.resize(msb - lsb + 1);
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::CAST(const int length) {
        auto result = data;
        result.resize(length, msb());
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::AND(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        return new BinaryBitVector(this->data & that->data);
    }

    BitVector *BinaryBitVector::XOR(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        return new BinaryBitVector(this->data ^ that->data);
    }

    BitVector *BinaryBitVector::OR(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        return new BinaryBitVector(this->data | that->data);
    }

    BitVector *BinaryBitVector::NOT() const {
        return new BinaryBitVector(~data);
    }

    BitVector *BinaryBitVector::ASHR(const int offset) const {
        auto result = data >> offset;
        if (msb()) {
            for (int i = 0; i != offset; ++i) {
                result.set(data.size() - 1 - i);
            }
        }
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::LSHR(const int offset) const {
        return new BinaryBitVector(data >> offset);
    }

    BitVector *BinaryBitVector::SHL(const int offset) const {
        return new BinaryBitVector(data << offset);
    }

    BitVector *BinaryBitVector::ADD(const BitVector &other) const {
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

    BitVector *BinaryBitVector::SUB(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (size() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(size(), this->toUL() - that->toUL()));
        }
        const auto neg = ~that->data;
        bool carry = true;
        boost::dynamic_bitset<> result(size());
        for (int i = 0; i != size(); ++i) {
            const bool sum = this->get(i) ^ neg[i];
            result.set(i, sum ^ carry);
            carry = this->get(i) & neg[i] | carry & sum;
        }
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::MUL(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (size() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(size(), this->toUL() * that->toUL()));
        }
        throw std::runtime_error("TODO: Not implemented");
    }

    BitVector *BinaryBitVector::DIV(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (size() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(size(), this->toUL() / that->toUL()));
        }
        throw std::runtime_error("TODO: Not implemented");
    }

    BitVector *BinaryBitVector::NEG() const {
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

    bool BinaryBitVector::operator==(const BitVector &other) const {
        if (other.size() == this->size()) {
            if (const auto *that = dynamic_cast<const BinaryBitVector *>(&other); that != nullptr) {
                return this->data == that->data;
            }
            for (int i = 0; i != size(); ++i) {
                if (this->get(i) != other.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
} // hson
