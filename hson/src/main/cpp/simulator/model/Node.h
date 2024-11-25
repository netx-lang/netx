#pragma once
#include <memory>
#include <utility>

#include "BitVector.h"
#include "Selector.h"
#include "SoureceLoc.h"

namespace hson {
    class Selector;

    class Node {
    public:
        void setIndex(const int index) {
            this->index = index;
        }

        void setTopology(const int index) {
            this->topology = index;
        }

        [[nodiscard]] int getIndex() const {
            return index;
        }

        [[nodiscard]] int getTopology() const {
            return topology;
        }

        virtual ~Node() = default;

    private:
        int index = 0;
        int topology = 0;
    };

    class ComputationNode final : public Node {
    public:
        explicit ComputationNode(std::string id): identifier(std::move(id)) {
        }

    private:
        std::string identifier;
    };

    class DataNode : public Node {
    public:
        [[nodiscard]] BitVector const *getValue() const {
            return value;
        }

        void setValue(BitVector *value) {
            if (this->value != nullptr) {
                delete this->value;
            }
            this->value = value;
        }

    private:
        BitVector *value = nullptr;
    };

    class WireNode final : public DataNode {
    public:
        WireNode(std::string identifier, const SourceLoc loc, const int width)
            : identifier(std::move(identifier)), loc(loc), width(width) {
        }

        [[nodiscard]] const std::string &id() const {
            return identifier;
        }

        [[nodiscard]] int getWidth() const {
            return width;
        }

    private:
        std::string identifier;

        SourceLoc loc;

        int width;
    };

    class WireSelectNode final : public DataNode {
    public:
        WireSelectNode(int base, Selector *selector);

        static WireSelectNode parse(int base, const std::string &str);

        ~WireSelectNode() override {
            delete selector;
        }

    private:
        int base;

        Selector *selector;
    };
} // hson
