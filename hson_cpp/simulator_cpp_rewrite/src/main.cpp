//
// Created by czm233 on 12/11/24.
//

#include <fstream>
#include <iostream>

#include "circuit/circuit_parser.h"

int main() {
    auto parsed = hson::parse_circuit(nlohmann::json::parse(std::ifstream("test.json")));

}
