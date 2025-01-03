#pragma once
#include <string>
#include <utility>

namespace nxon {
    struct SourceLoc {
        int line, column;

        static SourceLoc parse(const std::string &loc);
    };
} // nxon