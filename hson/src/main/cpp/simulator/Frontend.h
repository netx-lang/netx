#pragma once

#include <nlohmann/json.hpp>

#include "model/Circuit.h"

namespace hson {
    using json = nlohmann::json;

    class Frontend {
        json data;

        Circuit *circuit;

        explicit Frontend(const json &data);

        void parseData(const json &node) const;

        void createWireNode(const json &node, int width) const;

        void parseRelation(const json &node) const;

        void parseComponent(const json &node) const;

        void parseBuiltin(const json &node) const;

        [[nodiscard]] int resolveNode(const std::string &str) const;

        [[nodiscard]] std::vector<int> resolveNodeList(const json &top, const std::string &field) const;

        static std::pair<std::string, int> parseIdentifier(const std::string &str);

        enum NodeType {
            COMPONENT, BUILTIN, INSTANTIATE, CLOCK, WIRE
        };

        static NodeType typeOf(const std::string &type);

    public:
        static Frontend *loadString(const std::string &content);

        static Frontend *loadPath(const std::string &filename);

        Circuit *parse();
    };
} // hson
