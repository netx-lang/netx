#include "Circuit.h"

#include <queue>
#include <boost/iterator/zip_iterator.hpp>

namespace hson {
    Circuit::Circuit(const int size) {
        nodes = std::vector<Node>(size);
    }

    void Circuit::addInstanceNode(const int index, const Node &node) {
        nodes[index] = node;
        nodes[index].setIndex(index);
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

    Node *Circuit::resolveInput(const std::string &id) {
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

    Node *Circuit::resolveOutput(const std::string &id) {
        return resolveNode(output.at(id));
    }

    std::vector<Node *> Circuit::getOutputs() {
        std::vector<Node *> result;
        for (const auto &[fst, snd]: output) {
            result.push_back(resolveNode(snd));
        }
        return result;
    }

    Node *Circuit::resolveNode(const int index) {
        Node *ptr = &nodes[index];
        return ptr;
    }

    void Circuit::addRegister(const int clock, const Register &reg) {
        regManager.addRegister(clock, reg);
    }

    RegisterManager &Circuit::getRegisterManager() {
        return regManager;
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

    auto Circuit::successorsOf(const int node) {
        std::vector<Node *> successors;
        for (const auto element: outEdges[node]) {
            successors.push_back(resolveNode(element));
        }
        return successors;
    }

    auto Circuit::predecessorsOf(const int node) {
        std::vector<Node *> predecessors;
        for (const auto element: inEdges[node]) {
            predecessors.push_back(resolveNode(element));
        }
        return predecessors;
    }

    std::vector<Node> &Circuit::getAllNodes() {
        return nodes;
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
} // hson
