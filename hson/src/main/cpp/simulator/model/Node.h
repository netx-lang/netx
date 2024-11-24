#pragma once
#include <memory>

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

        virtual ~Node() = default;

    private:
        int index = 0;
    };

    class ComputationNode final : public Node {
    public:
        explicit ComputationNode(std::string id): id(std::move(id)) {
        }

    private:
        std::string id;
    };

    class DataNode : public Node {
    public:
        [[nodiscard]] const BitVector &getValue() const {
            return value;
        }

        void setValue(const BitVector &value) {
            this->value = value;
        }

    private:
        BitVector value;
    };

    class WireNode final : public DataNode {
    public:
        WireNode(std::string id, const SourceLoc loc, const int width)
            : id(std::move(id)), loc(loc), width(width) {
        }

    private:
        std::string id;

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
