#include "Selector.h"

#include <cmath>

namespace hson {
    int BitSelector::width() const {
        return 1;
    }

    BitVector * BitSelector::apply(const BitVector *value) const {
        return BinaryBitVector::fromBool(value->get(index));
    }

    BitVector * BitSelector::apply(const BitVector *base, const BitVector *value) const {
        if (value->width() != 1) {
            throw std::runtime_error("Invalid value");
        }
        const auto result = base->copyOf();
        result->set(index, value->lsb());
        return result;
    }

    int PartSelector::width() const {
        return std::abs(from - to) + 1;
    }

    BitVector * PartSelector::apply(const BitVector *value) const {
        return value->SELECT(from, to);
    }

    BitVector * PartSelector::apply(const BitVector *base, const BitVector *value) const {
        if (value->width() != width()) {
            throw std::runtime_error("Invalid value");
        }
        const auto result = base->copyOf();
        result->setRange(from, to, value);
        return result;
    }
} // hson