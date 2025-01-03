#include "SoureceLoc.h"

namespace nxon {
    SourceLoc SourceLoc::parse(const std::string &loc) {
        const auto pos = loc.find(':');
        return {std::stoi(loc.substr(0, pos)), std::stoi(loc.substr(pos + 1))};
    }
} // nxon
