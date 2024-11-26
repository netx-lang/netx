#pragma once
#include <queue>
#include <bits/random.h>

#include "model/Circuit.h"

namespace hson {
    struct comparator;

    class ValueSupplier {
        static constexpr bool RANDOM_INIT = false;

        std::random_device rd;

        auto mt = std::mt19937(rd());

    public:
        static BitVector *nextBitVector(int size);

        static BitVector *nextBit();

        int nextInt(int bound);
    };

    class Simulator {
    public:
        void run();

        void setInput(const std::string &id, BitVector *value) const;

        [[nodiscard]] BitVector const *getOutput(const std::string &id) const;

        explicit Simulator(Circuit *circuit);

        ~Simulator();

    private:
        Circuit *circuit;

        ValueSupplier supplier;

        class Comparator {
            static bool compare(const Node *a, const Node *b) {
                return a->getIndex() < b->getIndex();
            }
        };

        std::priority_queue<Node *, std::vector<Node *>, Comparator> worklist;
    };
} // hson
