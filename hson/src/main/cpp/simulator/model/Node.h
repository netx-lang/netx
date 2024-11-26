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
        void setIndex(int index);

        void setTopology(int index);

        [[nodiscard]] int getIndex() const;

        [[nodiscard]] int getTopology() const;

        virtual ~Node() = default;

    private:
        int index = 0;
        int topology = 0;
    };

    class ComputationNode final : public Node {
    public:
        explicit ComputationNode(std::string id);

    private:
        std::string identifier;
    };

    class DataNode : public Node {
    public:
        [[nodiscard]] BitVector const *getValue() const;

        void setValue(BitVector *value);

    private:
        BitVector *value = nullptr;
    };

    class WireNode final : public DataNode {
    public:
        WireNode(std::string identifier, SourceLoc loc, int width);

        [[nodiscard]] const std::string &id() const;

        [[nodiscard]] int getWidth() const;

    private:
        std::string identifier;

        SourceLoc loc;

        int width;
    };

    class WireSelectNode final : public DataNode {
    public:
        WireSelectNode(int base, Selector *selector);

        static WireSelectNode parse(int base, const std::string &str);

        ~WireSelectNode() override;

    private:
        int base;

        Selector *selector;
    };
} // hson
