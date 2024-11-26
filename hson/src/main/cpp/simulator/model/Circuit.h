#pragma once
#include <unordered_map>
#include <vector>

#include "Node.h"
#include "Register.h"

namespace hson {
    class Circuit {
        std::vector<Node*> nodes;
        std::vector<std::vector<int> > inEdges;
        std::vector<std::vector<int> > outEdges;

        RegisterManager regManager;
        std::unordered_map<std::string, int> input;
        std::unordered_map<std::string, int> output;

    public:
        explicit Circuit(int size);

        ~Circuit();

        int addRelationNode(Node *node);

        void addInstanceNode(int index, Node *node);

        void setInput(const std::vector<int> &inNodes);

        [[nodiscard]] Node *resolveInput(const std::string &id) const;

        [[nodiscard]] std::vector<Node *> getInputs();

        void setOutput(const std::vector<int> &outNodes);

        [[nodiscard]] Node *resolveOutput(const std::string &id) const;

        [[nodiscard]] std::vector<Node *> getOutputs();

        [[nodiscard]] Node *resolveNode(int index) const;

        void addRegister(int clock, const Register &reg);

        RegisterManager &manager();

        void addHyperEdge(int relation,
                          const std::vector<int> &inputs,
                          const std::vector<int> &outputs);

        void addEdge(int from, int to);

        [[nodiscard]] std::vector<Node *> successorsOf(const Node *node);

        [[nodiscard]] std::vector<Node *> predecessorsOf(const Node *node);

        [[nodiscard]] std::vector<Node *> getAllNodes();

        void topologicalSort();
    };
}
