#include <iostream>

#include "Frontend.h"
#include "Simulator.h"

using namespace hson;

int main() {
    const auto frontend = Frontend::loadPath(
        "../../src/test/resources/expanded/adder.json");
    const auto circuit = frontend->parse();
    const auto simulator = new Simulator(circuit);
    for (int i = 0; i != 256; ++i) {
        for (int j = 0; j != 256; ++j) {
            simulator->setInput("a", BinaryBitVector::fromUL(8, i));
            simulator->setInput("b", BinaryBitVector::fromUL(8, j));
            simulator->run();
            const auto expected = BinaryBitVector::fromUL(8, i + j);
            const auto actual = simulator->getOutput("c");
            assert(*expected == *actual);
        }
    }
    return 0;
}
