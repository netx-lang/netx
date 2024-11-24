#pragma once

namespace hson {
    class Selector {
    public:
        virtual ~Selector() = default;

        [[nodiscard]] virtual int width() const = 0;
    };

    class BitSelector final : public Selector {
    public:
        [[nodiscard]] int width() const override;

        explicit BitSelector(const int index): index(index) {
        }

    private:
        int index;
    };

    class PartSelector final : public Selector {
    public:
        [[nodiscard]] int width() const override;

        explicit PartSelector(const int from, const int to): from(from), to(to) {
        }

    private:
        int from, to;
    };
} // hson
