//
// Created by czm233 on 4/1/25.
//

#ifndef PARTITION_H
#define PARTITION_H

#include <map>
#include "../circuit.h"

namespace hxon {
    std::map<id_t, size_t> partition(const circuit& circuit);
}

#endif //PARTITION_H
