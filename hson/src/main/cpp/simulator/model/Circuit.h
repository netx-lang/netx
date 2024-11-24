#pragma once
#include <unordered_map>
#include <vector>

#include "Node.h"

namespace hson {
    class Register;

    class Circuit {
        std::vector<Node> nodes;
        std::vector<int> topology;
        std::vector<std::vector<int> > inEdges;
        std::vector<std::vector<int> > outEdges;
        std::unordered_map<int, std::vector<Register> > registers;

    public:
        explicit Circuit(int size);

        int addRelationNode(const Node &node);

        void addInstanceNode(int index, const Node &node);

        void addRegister(int clock, const Register &reg);

        void addHyperEdge(int relation,
                          const std::vector<int> &inputs,
                          const std::vector<int> &outputs);

        void addEdge(int from, int to);

        [[nodiscard]] std::vector<int>::const_iterator successorsOf(int node) const;

        [[nodiscard]] std::vector<int>::const_iterator predecessorsOf(int node) const;

        void topologicalSort();
    };

    class Register {
    public:
        Register(int input, int output, bool isPosTriggered);

    private:
        int input, output;

        bool isPosTriggered;
    };
}
