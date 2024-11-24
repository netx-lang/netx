#include "Selector.h"

#include <cmath>

namespace hson {
    int BitSelector::width() const {
        return 1;
    }

    int PartSelector::width() const {
        return std::abs(from - to) + 1;
    }

} // hson