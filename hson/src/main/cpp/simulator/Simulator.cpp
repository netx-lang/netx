#include "Simulator.h"

namespace hson {
    void Simulator::run() {

    }

    void Simulator::setInput(const std::string &id, const BitVector &value) const {
        const auto ptr = circuit->resolveInput(id);
        if (auto *data = reinterpret_cast<DataNode*>(ptr); data != nullptr) {
            data->setValue(value);
        }
        throw std::runtime_error("Invalid input");
    }

    BitVector Simulator::getOutput(const std::string &id) const {
        const auto ptr = circuit->resolveOutput(id);
        if (const auto *data = reinterpret_cast<DataNode*>(ptr); data != nullptr) {
            return data->getValue();
        }
        throw std::runtime_error("Invalid output");
    }

    Simulator::Simulator(Circuit *circuit) : circuit(circuit) {

    }

    Simulator::~Simulator() {
        delete circuit;
    }
} // hson