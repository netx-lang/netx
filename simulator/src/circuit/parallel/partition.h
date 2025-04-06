//
// Created by czm233 on 4/1/25.
//

#ifndef PARTITION_H
#define PARTITION_H

#include <map>
#include <set>
#include <vector>
#include "../circuit.h"

namespace hxon {
    struct partitioned_circuit {
        size_t num_parts;
        std::map<id_t, size_t> belongs;
        std::map<id_t, std::set<size_t>> relate;
        id_set clocks;
        std::vector<circuit> circuit_parts;
    };

    partitioned_circuit partition(const circuit& circuit);
}

#endif //PARTITION_H
