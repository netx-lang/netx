#include "Circuit.h"

#include <queue>

#include "Node.h"

namespace hson {
    Circuit::Circuit(const int size) {
        nodes = std::vector<Node>(size);
    }

    void Circuit::addInstanceNode(const int index, const Node &node) {
        nodes[index] = node;
        nodes[index].setIndex(index);
    }

    void Circuit::setInput(const std::vector<int> &inNodes) {
        for (const int index : inNodes) {
            const auto ptr = resolveNode(index);
            if (const auto *wire = reinterpret_cast<WireNode*>(ptr); wire != nullptr) {
                input[wire->id()] = index;
            }
        }
    }

    Node *Circuit::resolveInput(const std::string &id) {
        return resolveNode(input.at(id));
    }

    void Circuit::setOutput(const std::vector<int> &outNodes) {
        for (const int index : outNodes) {
            const auto ptr = resolveNode(index);
            if (const auto *wire = reinterpret_cast<WireNode*>(ptr); wire != nullptr) {
                output[wire->id()] = index;
            }
        }
    }

    Node *Circuit::resolveOutput(const std::string &id) {
        return resolveNode(output.at(id));
    }

    Node *Circuit::resolveNode(const int index) {
        Node *ptr = &nodes[index];
        return ptr;
    }

    void Circuit::addRegister(const int clock, const Register &reg) {
        registers[clock].push_back(reg);
    }

    int Circuit::addRelationNode(const Node &node) {
        const int index = static_cast<int>(nodes.size());
        nodes.push_back(node);
        nodes.back().setIndex(index);
        return index;
    }

    void Circuit::addEdge(const int from, const int to) {
        if (from >= outEdges.size()) {
            outEdges.resize(from + 1, {});
        }
        if (to >= inEdges.size()) {
            inEdges.resize(to + 1, {});
        }
        inEdges[to].push_back(from);
        outEdges[from].push_back(to);
    }

    std::vector<int>::const_iterator Circuit::successorsOf(const int node) const {
        return outEdges[node].begin();
    }

    std::vector<int>::const_iterator Circuit::predecessorsOf(const int node) const {
        return inEdges[node].begin();
    }

    void Circuit::topologicalSort() {
        std::queue<int> queue;
        auto degree = std::vector(nodes.size(), 0);
        for (int i = 0; i < nodes.size(); i++) {
            degree[i] = static_cast<int>(inEdges[i].size());
            if (degree[i] == 0) {
                queue.push(i);
            }
        }
        int indexer = 0;
        while (!queue.empty()) {
            const int head = queue.front();
            resolveNode(head)->setTopology(indexer++);
            queue.pop();
            for (auto &successor: outEdges[head]) {
                if (--degree[successor] == 0) {
                    queue.push(successor);
                }
            }
        }
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
