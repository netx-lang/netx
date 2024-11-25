#pragma once
#include <queue>

#include "model/Circuit.h"

namespace hson {
    struct comparator;

    class Simulator {
    public:
        void run();

        void setInput(const std::string &id, BitVector *value) const;

        [[nodiscard]] BitVector const *getOutput(const std::string &id) const;

        explicit Simulator(Circuit *circuit);

        ~Simulator();
    private:
        Circuit *circuit;

        std::priority_queue<Node*, std::vector<Node*>, comparator> worklist;

    };

    struct comparator {
        bool operator()(Node *lhs, Node *rhs) const {
            return lhs->getTopology() < rhs->getTopology();
        }
    };
} // hson
