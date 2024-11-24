#include "Circuit.h"

#include "Node.h"

namespace hson {
    Circuit::Circuit(const int size) {
        nodes = std::vector<Node>(size);
    }

    void Circuit::addInstanceNode(const int index, const Node &node) {
        nodes[index] = node;
    }

    void Circuit::addRegister(const int clock, const Register &reg) {
        registers[clock].push_back(reg);
    }

    int Circuit::addRelationNode(const Node &node) {
        nodes.push_back(node);
        return static_cast<int>(nodes.size()) - 1;
    }

    void Circuit::addEdge(int from, int to) {
        // TODO:
    }

    void Circuit::addHyperEdge(const int relation,
                               const std::vector<int> &inputs,
                               const std::vector<int> &outputs) {
        for (auto &input: inputs) {
            addEdge(input, relation);
        }
        for (auto &output: outputs) {
            addEdge(relation, output);
        }
    }

    Register::Register(const int input, const int output, const bool isPosTriggered) : input(input), output(output),
        isPosTriggered(isPosTriggered) {
    }
} // hson
