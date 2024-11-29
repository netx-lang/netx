#pragma once
#include "BitVector.h"

namespace hson {
    class Selector {
    public:
        virtual ~Selector() = default;

        [[nodiscard]] virtual int width() const = 0;

        [[nodiscard]] virtual BitVector *apply(const BitVector *value) const = 0;

        [[nodiscard]] virtual BitVector *apply(const BitVector *base, const BitVector *value) const = 0;
    };

    class BitSelector final : public Selector {
    public:
        [[nodiscard]] int width() const override;

        explicit BitSelector(const int index): index(index) {
        }

        [[nodiscard]] BitVector * apply(const BitVector *value) const override;

        [[nodiscard]] BitVector * apply(const BitVector *base, const BitVector *value) const override;

    private:
        int index;
    };

    class PartSelector final : public Selector {
    public:
        [[nodiscard]] int width() const override;

        explicit PartSelector(const int from, const int to): from(from), to(to) {
        }

        [[nodiscard]] BitVector * apply(const BitVector *value) const override;

        [[nodiscard]] BitVector * apply(const BitVector *base, const BitVector *value) const override;

    private:
        int from, to;
    };
} // hson
