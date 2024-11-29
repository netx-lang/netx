#pragma once
#include <string>
#include <utility>

namespace hson {
    struct SourceLoc {
        int line, column;

        static SourceLoc parse(const std::string &loc);
    };
} // hson