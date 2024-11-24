#include "Node.h"

#include "Selector.h"

namespace hson {
    WireSelectNode::WireSelectNode(const int base, Selector *selector)
    : base(base), selector(selector) {
    }

    WireSelectNode WireSelectNode::parse(int base, const std::string &str) {
        if (const auto pos = str.find(':'); pos == std::string::npos) {
            return {base, new BitSelector(std::stoi(str))};
        } else {
            return {base, new PartSelector(
                    std::stoi(str.substr(0, pos)),
                    std::stoi(str.substr(pos + 1)))};
        }
    }
} // hson