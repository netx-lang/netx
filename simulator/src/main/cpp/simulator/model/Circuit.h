#pragma once
#include <unordered_map>
#include <vector>

#include "Node.h"

namespace nxon {
    class Circuit {
        std::vector<Node*> nodes;
        std::vector<std::vector<int> > inEdges;
        std::vector<std::vector<int> > outEdges;

        std::unordered_map<std::string, int> input;
        std::unordered_map<std::string, int> output;
        std::unordered_map<int, std::vector<Register> > registers;

    public:
        explicit Circuit(int size);

        ~Circuit();

        int addNode(Node *node);

        void addNode(int index, Node *node);

        void setInput(const std::vector<int> &inNodes);

        [[nodiscard]] Node *resolveInput(const std::string &id) const;

        [[nodiscard]] std::vector<Node *> getInputs();

        void setOutput(const std::vector<int> &outNodes);

        [[nodiscard]] Node *resolveOutput(const std::string &id) const;

        [[nodiscard]] std::vector<Node *> getOutputs();

        [[nodiscard]] Node *resolveNode(int index) const;

        void addClock(int clock);

        void addRegister(int clock, const Register &reg);

        void addHyperEdge(int relation,
                          const std::vector<int> &inputs,
                          const std::vector<int> &outputs);

        void addEdge(int from, int to);

        [[nodiscard]] std::vector<Node *> successorsOf(const Node *node);

        [[nodiscard]] std::vector<Node *> predecessorsOf(const Node *node);

        [[nodiscard]] std::vector<Node *> getAllNodes();

        [[nodiscard]] std::vector<Register> getRegisters(const ClockNode *clock);

        void topologicalSort();
    };
}
