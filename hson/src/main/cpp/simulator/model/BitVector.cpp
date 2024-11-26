#include "BitVector.h"

#include <format>

namespace hson {
    const BinaryBitVector *BinaryBitVector::requiresValidOperand(const BitVector &other) const {
        if (const auto *that = dynamic_cast<const BinaryBitVector *>(&other); that != nullptr) {
            if (that->width() == this->width()) {
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
                    if (index == size) {
                        return new BinaryBitVector(result);
                    }
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

    BinaryBitVector *BinaryBitVector::fromUL(const unsigned int width, const unsigned long value) {
        return new BinaryBitVector(boost::dynamic_bitset(width, value));
    }

    BitVector *BinaryBitVector::copyOf() const {
        return new BinaryBitVector(boost::dynamic_bitset(data));
    }


    int BinaryBitVector::width() const {
        return static_cast<int>(data.size());
    }

    bool BinaryBitVector::toBool() const {
        return data.all();
    }

    signed long BinaryBitVector::toSL() const {
        return static_cast<signed long>(data.to_ulong());
    }

    unsigned long BinaryBitVector::toUL() const {
        return data.to_ulong();
    }

    bool BinaryBitVector::get(const int index) const {
        return data.test(index);
    }

    bool BinaryBitVector::msb() const {
        return get(width() - 1);
    }

    bool BinaryBitVector::lsb() const {
        return get(0);
    }

    void BinaryBitVector::set(const int index, const bool value) {
        data.set(index, value);
    }

    void BinaryBitVector::setRange(const int from, const int to, const BitVector *value) {
        const int msb = std::max(from, to), lsb = std::min(from, to);
        if (value->width() != msb - lsb + 1) {
            throw std::runtime_error("Invalid value");
        }
        for (int i = lsb; i <= msb; ++i) {
            set(i, value->get(i - lsb));
        }
    }

    BitVector *BinaryBitVector::SELECT(const int from, const int to) const {
        const int msb = std::max(from, to), lsb = std::min(from, to);
        auto result = boost::dynamic_bitset(data >> lsb);
        result.resize(msb - lsb + 1);
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::CONCAT(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        const auto result = new BinaryBitVector(boost::dynamic_bitset<>(this->width() + that->width()));
        result->setRange(that->width() - 1, 0, that);
        result->setRange(result->width() - 1, that->width(), this);
        return result;
    }

    BitVector *BinaryBitVector::CAST(const int length) const {
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

    BitVector *BinaryBitVector::NAND(const BitVector &other) const {
        const auto value = AND(other);
        const auto result = value->NOT();
        delete value;
        return result;
    }

    BitVector *BinaryBitVector::XNOR(const BitVector &other) const {
        const auto value = XOR(other);
        const auto result = value->NOT();
        delete value;
        return result;
    }

    BitVector *BinaryBitVector::NOR(const BitVector &other) const {
        const auto value = OR(other);
        const auto result = value->NOT();
        delete value;
        return result;
    }

    BitVector *BinaryBitVector::NOT() const {
        return new BinaryBitVector(~data);
    }

    BitVector *BinaryBitVector::ASHR(const unsigned long offset) const {
        auto result = data >> offset;
        if (msb()) {
            for (int i = 0; i != offset; ++i) {
                result.set(data.size() - 1 - i);
            }
        }
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::LSHR(const unsigned long offset) const {
        return new BinaryBitVector(data >> offset);
    }

    BitVector *BinaryBitVector::SHL(const unsigned long offset) const {
        return new BinaryBitVector(data << offset);
    }

    BitVector *BinaryBitVector::ADD(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(width(), this->toUL() + that->toUL()));
        }
        bool carry = false;
        boost::dynamic_bitset<> result(width());
        for (int i = 0; i != width(); ++i) {
            const bool sum = this->get(i) ^ that->get(i);
            result.set(i, sum ^ carry);
            carry = this->get(i) & that->get(i) | carry & sum;
        }
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::SUB(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(width(), this->toUL() - that->toUL()));
        }
        const auto neg = ~that->data;
        bool carry = true;
        boost::dynamic_bitset<> result(width());
        for (int i = 0; i != width(); ++i) {
            const bool sum = this->get(i) ^ neg[i];
            result.set(i, sum ^ carry);
            carry = this->get(i) & neg[i] | carry & sum;
        }
        return new BinaryBitVector(result);
    }

    BitVector *BinaryBitVector::MUL(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(width(), this->toUL() * that->toUL()));
        }
        throw std::runtime_error("TODO: Not implemented");
    }

    BitVector *BinaryBitVector::DIV(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return new BinaryBitVector(
                boost::dynamic_bitset(width(), this->toUL() / that->toUL()));
        }
        throw std::runtime_error("TODO: Not implemented");
    }

    BitVector *BinaryBitVector::NEG() const {
        boost::dynamic_bitset<> result = ~data;
        for (int i = 0; i != width(); ++i) {
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
        if (other.width() == this->width()) {
            if (const auto *that = dynamic_cast<const BinaryBitVector *>(&other); that != nullptr) {
                return this->data == that->data;
            }
            for (int i = 0; i != width(); ++i) {
                if (this->get(i) != other.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    bool BinaryBitVector::operator!=(const BitVector &other) const {
        return !(*this == other);
    }

    bool BinaryBitVector::SLT(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return this->toSL() < that->toSL();
        }
        if (this->msb() != that->msb()) {
            return this->msb();
        }
        for (int i = width() - 1; i >= 0; --i) {
            if (this->get(i) != that->get(i)) {
                return this->get(i) && !that->get(i);
            }
        }
        return false;
    }

    bool BinaryBitVector::SLE(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return this->toSL() <= that->toSL();
        }
        if (this->msb() != that->msb()) {
            return this->msb();
        }
        for (int i = width() - 1; i >= 0; --i) {
            if (this->get(i) != that->get(i)) {
                return this->get(i) && !that->get(i);
            }
        }
        return true;
    }

    bool BinaryBitVector::ULT(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return this->toUL() < that->toUL();
        }
        for (int i = width() - 1; i >= 0; --i) {
            if (this->get(i) != that->get(i)) {
                return this->get(i) && !that->get(i);
            }
        }
        return false;
    }

    bool BinaryBitVector::ULE(const BitVector &other) const {
        const auto that = requiresValidOperand(other);
        if (width() <= LONG_SIZE) {
            return this->toUL() <= that->toUL();
        }
        for (int i = width() - 1; i >= 0; --i) {
            if (this->get(i) != that->get(i)) {
                return this->get(i) && !that->get(i);
            }
        }
        return true;
    }

    bool BinaryBitVector::SGT(const BitVector &other) const {
        return !SLE(other);
    }

    bool BinaryBitVector::SGE(const BitVector &other) const {
        return !SLT(other);
    }

    bool BinaryBitVector::UGT(const BitVector &other) const {
        return !ULE(other);
    }

    bool BinaryBitVector::UGE(const BitVector &other) const {
        return !ULT(other);
    }

    std::string BinaryBitVector::print() const {
        std::string result = std::to_string(width());
        result.reserve(result.size() + width() + 2);
        result.append("'b");
        for (int i = width() - 1; i >= 0; --i) {
            result.append(get(i) ? "1" : "0");
        }
        return result;
    }
} // hson
