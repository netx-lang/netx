#include "Frontend.h"

#include <fstream>
#include <iostream>
#include <utility>

#include "model/Node.h"

namespace hson {
    Frontend::Frontend(const json &data): data(data) {
        const int count = data.at("totalData");
        circuit = new Circuit(count);
    }

    Frontend *Frontend::loadString(const std::string &content) {
        return new Frontend(json::parse(content));
    }

    Frontend *Frontend::loadPath(const std::string &filename) {
        if (std::ifstream read(filename); read.is_open()) {
            return new Frontend(json::parse(read));
        }
        throw std::runtime_error("File not found");
    }

    Circuit *Frontend::parse() {
        parseData(data.at("design"));
        parseRelation(data.at("design"));

        circuit->setInput(resolveNodeList(data.at("design"), "input"));
        circuit->setOutput(resolveNodeList(data.at("design"), "output"));
        return circuit;
    }

    void Frontend::parseData(const json &node) const { // NOLINT(*-no-recursion)
        switch (typeOf(node.at("type"))) {
            case CLOCK: createClockNode(node);
                break;
            case WIRE: createWireNode(node, node.at("width"));
                break;
            default: {
                if (node.contains("members")) {
                    for (auto &member: node.at("members")) {
                        parseData(member);
                    }
                }
                break;
            }
        }
    }

    void Frontend::parseRelation(const json &node) const { // NOLINT(*-no-recursion)
        switch (typeOf(node.at("type"))) {
            case COMPONENT: parseComponent(node);
                break;
            case BUILTIN: parseBuiltin(node);
                break;
            default: return;
        }
        if (node.contains("members")) {
            for (auto &member: node.at("members")) {
                parseRelation(member);
            }
        }
    }

    void Frontend::parseComponent(const json &node) const {
        const int relation = circuit->addNode(new ComputationNode(node.at("id")));
        circuit->addHyperEdge(relation,
                              resolveNodeList(node, "input"),
                              resolveNodeList(node, "output"));
    }

    void Frontend::parseBuiltin(const json &node) const {
        const std::string id = node.at("id");
        const auto inputs = resolveNodeList(node, "input");
        const auto outputs = resolveNodeList(node, "output");
        if (id == "REGISTER") {
            json attr = node.at("attribute");
            const int clock = resolveNode(attr.at("clock"));
            const bool isPosTriggered = attr.at("isPosTriggered") == "true";
            circuit->addRegister(clock, Register(inputs[0], outputs[0], isPosTriggered));
        } else {
            const int relation = circuit->addNode(new ComputationNode(id));
            circuit->addHyperEdge(relation, inputs, outputs);
        }
    }


    int Frontend::resolveNode(const std::string &str) const {
        if (const auto pos = str.find('['); pos == std::string::npos) {
            if (const auto at = str.find('@'); at == std::string::npos) {
                return circuit->addNode(new ConstantNode(BinaryBitVector::parse(str)));
            } else {
                return std::stoi(str.substr(at + 1));
            }
        } else {
            auto [_, index] = parseIdentifier(str.substr(0, pos));
            const auto selectorStr = str.substr(pos + 1, str.size() - pos - 2);
            const int selectIndex = circuit->addNode(WireSelectNode::parse(index, selectorStr));
            circuit->addEdge(index, selectIndex);
            return selectIndex;
        }
    }

    std::vector<int> Frontend::resolveNodeList(const json &top, const std::string &field) const {
        std::vector<int> outputs;
        for (auto &element: top.at(field)) {
            outputs.push_back(resolveNode(element));
        }
        return outputs;
    }

    std::pair<std::string, int> Frontend::parseIdentifier(const std::string &str) {
        const auto pos = str.find('@');
        return {str.substr(0, pos), std::stoi(str.substr(pos + 1))};
    }

    void Frontend::createWireNode(const json &node, const int width) const {
        const auto loc = SourceLoc::parse(node.at("loc"));
        auto [name, index] = parseIdentifier(node.at("id"));
        circuit->addNode(index, new WireNode(name, loc, width));
    }

    void Frontend::createClockNode(const json &node) const {
        const auto loc = SourceLoc::parse(node.at("loc"));
        auto [name, index] = parseIdentifier(node.at("id"));
        circuit->addNode(index, new ClockNode(name, loc));
        circuit->addClock(index);
    }

    Frontend::NodeType Frontend::typeOf(const std::string &type) {
        static std::unordered_map<std::string, NodeType> map = {
            {"instantiate", INSTANTIATE},
            {"component", COMPONENT},
            {"builtin", BUILTIN},
            {"clock", CLOCK},
            {"wire", WIRE}
        };
        return map[type];
    }
} // hson
