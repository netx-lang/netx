#pragma once
#include <queue>
#include <random>

#include "model/Circuit.h"

namespace hson {
    struct comparator;

    class ValueSupplier {
        bool RANDOM_INIT;

        std::random_device rd;

        std::mt19937 mt = std::mt19937(rd());

        std::uniform_int_distribution<> bitGen = std::uniform_int_distribution(0, 1);

    public:
        explicit ValueSupplier(const bool RANDOM_INIT): RANDOM_INIT(RANDOM_INIT) {
        }

        BitVector *nextBitVector(int size);

        BitVector *nextBit();

        int nextInt(int bound);
    };

    class Simulator {
    public:
        void run();

        void dataNodeStep(const DataNode *node);

        void computationNodeStep(const ComputationNode *node);

        void step();

        static bool isBuiltin(const std::string &id);

        bool clockStep();

        ClockNode *pickClock();

        static std::vector<BitVector *> evaluate(const std::string &id,
                                                 const std::vector<const BitVector *> &input);

        void setInput(const std::string &id, BitVector *value);

        [[nodiscard]] BitVector const *getOutput(const std::string &id) const;

        explicit Simulator(Circuit *circuit, bool DO_VERIFICATION = false, bool RANDOM_INIT = false);

        ~Simulator();

    private:
        bool DO_VERIFICATION;

        Circuit *circuit;

        ValueSupplier supplier;

        std::vector<ClockNode *> clocks;

        static bool cmp(const Node* ptr1, const Node* ptr2) {
            return ptr1->getTopology() < ptr2->getTopology();
        }

        std::priority_queue<Node *, std::vector<Node *>, std::function<bool(const Node*, const Node*)>> worklist;
    };
} // hson
