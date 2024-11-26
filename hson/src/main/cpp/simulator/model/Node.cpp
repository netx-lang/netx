#include "Node.h"

#include "Selector.h"

namespace hson {
    void Node::setIndex(const int index) {
        this->index = index;
    }

    void Node::setTopology(const int index) {
        this->topology = index;
    }

    int Node::getIndex() const {
        return index;
    }

    int Node::getTopology() const {
        return topology;
    }

    ComputationNode::ComputationNode(std::string id): identifier(std::move(id)) {
    }

    BitVector const *DataNode::getValue() const {
        return value;
    }

    void DataNode::setValue(BitVector *value) {
        delete this->value;
        this->value = value;
    }

    WireNode::WireNode(std::string identifier, const SourceLoc loc, const int width)
        : identifier(std::move(identifier)), loc(loc), width(width) {
    }

    const std::string &WireNode::id() const {
        return identifier;
    }

    int WireNode::getWidth() const {
        return width;
    }

    WireSelectNode::WireSelectNode(const int base, Selector *selector)
        : base(base), selector(selector) {
    }

    WireSelectNode WireSelectNode::parse(int base, const std::string &str) {
        if (const auto pos = str.find(':'); pos == std::string::npos) {
            return {base, new BitSelector(std::stoi(str))};
        } else {
            return {
                base, new PartSelector(
                    std::stoi(str.substr(0, pos)),
                    std::stoi(str.substr(pos + 1)))
            };
        }
    }

    WireSelectNode::~WireSelectNode() {
        delete selector;
    }
} // hson
