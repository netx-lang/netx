#pragma once
#include <queue>
#include <random>
#include <unordered_set>

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

    class ScheduleQueue {
        std::vector<Node *> rep = {nullptr};
        std::unordered_set<Node *> hash;

    public:
        [[nodiscard]] bool empty() const {
            return rep.size() == 1;
        }

        void push(Node *node) {
            if (hash.contains(node)) {
                return;
            }

            size_t index = rep.size();
            rep.push_back(nullptr);
            while (index > 1) {
                if (const size_t parent = index / 2; !compare(rep[parent], node)) {
                    rep[index] = rep[parent];
                    index = parent;
                } else {
                    break;
                }
            }
            rep[index] = node;
            hash.insert(node);
        }

        Node *poll() {
            Node *result = rep[1];
            hash.erase(result);

            Node *last = rep.back();
            rep.pop_back();
            if (rep.size() > 1) {
                size_t index = 1, son;
                const size_t size = rep.size();
                while ((son = index * 2) < size) {
                    if (son + 1 < size && compare(rep[son + 1], rep[son])) {
                        son++;
                    }
                    if (compare(last, rep[son])) {
                        break;
                    }
                    rep[index] = rep[son];
                    index = son;
                }
                rep[index] = last;
            }
            return result;
        }

        static bool compare(const Node *lhs, const Node *rhs) {
            return lhs->getTopology() < rhs->getTopology();
        }
    };

    class Simulator {
    public:
        void run();

        void dataNodeStep(const DataNode *node);

        static bool needPropagate(const BitVector *lhs, const BitVector *rhs);

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

        ScheduleQueue worklist;
    };
} // hson
