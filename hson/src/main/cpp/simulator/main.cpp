#include "Frontend.h"

using namespace hson;

int main() {
    const auto frontend = Frontend::loadPath(
        "../../src/test/resources/expanded/adder.json");
    frontend->parse();
    return 0;
}