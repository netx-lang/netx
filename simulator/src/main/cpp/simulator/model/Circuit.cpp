#include "Circuit.h"

#include <queue>

namespace nxon {
    Circuit::Circuit(const int size) {
        nodes = std::vector<Node*>(size);
    }

    Circuit::~Circuit() {
        for (const auto node : nodes) {
            delete node;
        }
    }

    void Circuit::addNode(const int index, Node *node) {
        node->setIndex(index);
        nodes[index] = node;
    }

    void Circuit::setInput(const std::vector<int> &inNodes) {
        for (const int index: inNodes) {
            const auto ptr = resolveNode(index);
            if (const auto *wire = reinterpret_cast<WireNode *>(ptr); wire != nullptr) {
                input[wire->id()] = index;
            } else {
                throw std::runtime_error("Input node must be a wire");
            }
        }
    }

    Node *Circuit::resolveInput(const std::string &id) const {
        return resolveNode(input.at(id));
    }

    std::vector<Node *> Circuit::getInputs() {
        std::vector<Node *> result;
        for (const auto &[fst, snd]: input) {
            result.push_back(resolveNode(snd));
        }
        return result;
    }

    void Circuit::setOutput(const std::vector<int> &outNodes) {
        for (const int index: outNodes) {
            const auto ptr = resolveNode(index);
            if (const auto *wire = reinterpret_cast<WireNode *>(ptr); wire != nullptr) {
                output[wire->id()] = index;
            } else {
                throw std::runtime_error("Input node must be a wire");
            }
        }
    }

    Node *Circuit::resolveOutput(const std::string &id) const {
        return resolveNode(output.at(id));
    }

    std::vector<Node *> Circuit::getOutputs() {
        std::vector<Node *> result;
        for (const auto &[fst, snd]: output) {
            result.push_back(resolveNode(snd));
        }
        return result;
    }

    Node *Circuit::resolveNode(const int index) const {
        return nodes[index];
    }

    void Circuit::addClock(const int clock) {
        registers[clock] = {};
    }

    void Circuit::addRegister(const int clock, const Register &reg) {
        registers[clock].push_back(reg);
    }

    int Circuit::addNode(Node *node) {
        const int index = static_cast<int>(nodes.size());
        node->setIndex(index);
        nodes.push_back(node);
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

    std::vector<Node *> Circuit::successorsOf(const Node *node) {
        std::vector<Node *> successors;
        for (const auto element: outEdges[node->getIndex()]) {
            successors.push_back(resolveNode(element));
        }
        return successors;
    }

    std::vector<Node *> Circuit::predecessorsOf(const Node *node) {
        std::vector<Node *> predecessors;
        for (const auto element: inEdges[node->getIndex()]) {
            predecessors.push_back(resolveNode(element));
        }
        return predecessors;
    }

    std::vector<Node *> Circuit::getAllNodes() {
        return nodes;
    }

    std::vector<Register> Circuit::getRegisters(const ClockNode *clock) {
        return registers.at(clock->getIndex());
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
            queue.pop();
            resolveNode(head)->setTopology(indexer++);
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
} // nxon
