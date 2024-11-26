#include "Simulator.h"

namespace hson {
    void Simulator::run() {
        for (auto node: circuit->getAllNodes()) {
            if (auto *wire = dynamic_cast<WireNode *>(&node); wire != nullptr) {
                wire->setValue(supplier.nextBitVector(wire->getWidth()));
            }
        }
    }

    void Simulator::setInput(const std::string &id, BitVector *value) const {
        const auto ptr = circuit->resolveInput(id);
        if (auto *data = reinterpret_cast<DataNode *>(ptr); data != nullptr) {
            data->setValue(value);
        }
        throw std::runtime_error("Invalid input");
    }

    BitVector const *Simulator::getOutput(const std::string &id) const {
        const auto ptr = circuit->resolveOutput(id);
        if (const auto *data = reinterpret_cast<DataNode *>(ptr); data != nullptr) {
            return data->getValue();
        }
        throw std::runtime_error("Invalid output");
    }

    Simulator::Simulator(Circuit *circuit) : circuit(circuit) {
    }

    Simulator::~Simulator() {
        delete circuit;
    }

    BitVector *ValueSupplier::nextBitVector(const int size) {
        if (RANDOM_INIT) {
            if (size <= sizeof(unsigned long) * 8) {
                return BinaryBitVector::fromUL(
                    std::uniform_random_bit_generator(0, 1 << size)(mt));
            }
            boost::dynamic_bitset<> data(size);
            for (int i = 0; i != size; ++i) {
                if (std::uniform_random_bit_generator(0, 1)(mt)) {
                    data.set(i);
                }
            }
            return new BinaryBitVector(data);
        }
        return new BinaryBitVector(boost::dynamic_bitset<>(size, 0));
    }

    BitVector *ValueSupplier::nextBit() {
        if (RANDOM_INIT) {
            return BinaryBitVector::fromBool(std::uniform_random_bit_generator(0, 1)(mt));
        }
        return BinaryBitVector::fromBool(false);
    }

    int ValueSupplier::nextInt(const int bound) {
        if (RANDOM_INIT) {
            return std::uniform_int_distribution(0, bound)(mt);
        }
        return 0;
    }
} // hson
