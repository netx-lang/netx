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

        [[nodiscard]] const std::string &id() const;

    private:
        std::string identifier;
    };

    class DataNode : public Node {
    public:
        [[nodiscard]] virtual BitVector const *getValue() const;

        [[nodiscard]] virtual int getWidth() const = 0;

        void setValue(BitVector *value);

        ~DataNode() override {
            delete value;
        }

    private:
        BitVector *value = nullptr;
    };

    class WireNode final : public DataNode {
    public:
        WireNode(std::string identifier, SourceLoc loc, int width);

        [[nodiscard]] const std::string &id() const;

        [[nodiscard]] int getWidth() const override;

    private:
        std::string identifier;

        SourceLoc loc;

        int width;
    };

    class ConstantNode final : public DataNode {
    public:
        explicit ConstantNode(BitVector *value);

        [[nodiscard]] int getWidth() const override;

        [[nodiscard]] const BitVector *getValue() const;
    private:
        BitVector *value;
    };

    class ClockNode final : public DataNode {
    public:
        ClockNode(std::string identifier, SourceLoc loc);

        [[nodiscard]] const std::string &id() const;

        [[nodiscard]] int getWidth() const override;

    private:
        std::string identifier;

        SourceLoc loc;
    };

    class WireSelectNode final : public DataNode {
    public:
        WireSelectNode(int base, Selector *selector);

        static WireSelectNode *parse(int base, const std::string &str);

        [[nodiscard]] int getBase() const {
            return base;
        }

        [[nodiscard]] Selector * getSelector() const {
            return selector;
        }

        ~WireSelectNode() override;

        [[nodiscard]] int getWidth() const override;

    private:
        int base;

        Selector *selector;
    };
} // hson
