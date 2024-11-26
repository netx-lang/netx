#include "Simulator.h"

#include <iostream>
#include <unordered_set>

namespace hson {
    void Simulator::run() {
        bool hasProgress = true;
        while (hasProgress) {
            while (!worklist.empty()) {
                step();
            }
            hasProgress = clockStep();
        }
    }

    void Simulator::step() {
        const auto node = worklist.top();
        worklist.pop();
        if (const auto compute = dynamic_cast<ComputationNode *>(node); compute != nullptr) {
            computationNodeStep(compute);
        } else if (const auto data = dynamic_cast<DataNode *>(node); data != nullptr) {
            dataNodeStep(data);
        } else {
            throw std::runtime_error("Invalid node");
        }
    }

    void Simulator::dataNodeStep(const DataNode * const node) {
        for (const auto successor: circuit->successorsOf(node)) {
            if (const auto compute = dynamic_cast<ComputationNode *>(successor); compute != nullptr) {
                if (DO_VERIFICATION || isBuiltin(compute->id())) {
                    worklist.push(successor);
                }
            } else if (const auto select = dynamic_cast<WireSelectNode *>(successor); select != nullptr) {
                const auto value = node->getValue();
                const auto selected = select->getSelector()->apply(value);
                if (needPropagate(select->getValue(), selected)) {
                    worklist.push(successor);
                }
                select->setValue(selected);
            } else {
                throw std::runtime_error("Invalid successor");
            }
        }
    }

    bool Simulator::needPropagate(const BitVector *lhs, const BitVector *rhs) {
        if (lhs == nullptr || rhs == nullptr) {
            return true;
        }
        return *lhs != *rhs;
    }

    void Simulator::computationNodeStep(const ComputationNode * const node) {
        if (isBuiltin(node->id())) {
            std::vector<const BitVector *> arguments;
            for (const auto predecessor : circuit->predecessorsOf(node)) {
                if (const auto data = dynamic_cast<DataNode *>(predecessor); data != nullptr) {
                    arguments.push_back(data->getValue());
                } else {
                    throw std::runtime_error("Invalid predecessor");
                }
            }

            // if (arguments[0] == NULL) {
            //     auto shit = circuit->predecessorsOf(node);
            // }

            int indexer = 0;
            const auto result = evaluate(node->id(), arguments);
            for (const auto successor : circuit->successorsOf(node)) {
                const auto data = static_cast<DataNode *>(successor); // NO-LINT: NOLINT(cppcoreguidelines-pro-type-static-cast-downcast)
                auto value = result[indexer++];

                if (data->getWidth() != value->width()) {
                    const auto casted = value->CAST(data->getWidth());
                    delete value;
                    value = casted;
                }

                if (needPropagate(data->getValue(), value)) {
                    worklist.push(successor);
                    if (const auto select = dynamic_cast<WireSelectNode *>(data); select != nullptr) {
                        const auto base = static_cast<DataNode *>(circuit->resolveNode(select->getBase())); // NO-LINT: NOLINT(cppcoreguidelines-pro-type-static-cast-downcast)
                        const auto newBase = select->getSelector()->apply(base->getValue(), value);
                        if (needPropagate(base->getValue(), newBase)) {
                            worklist.push(base);
                        }
                        base->setValue(newBase);
                    }
                }
                data->setValue(value);
            }
        } else if (DO_VERIFICATION) {
            throw std::runtime_error("TODO: not implemented");
        }
    }

    bool Simulator::isBuiltin(const std::string &id) {
        static std::unordered_set<std::string> BUILTIN = {
            "AND", "OR", "XOR", "NOT"
        };
        return BUILTIN.contains(id);
    }

    bool Simulator::clockStep() {
        const auto clock = pickClock();
        if (clock == nullptr) {
            return false;
        }

        std::vector<std::pair<DataNode*, BitVector*>> incoming;
        clock->setValue(clock->getValue()->NOT());
        const bool posEdge = clock->getValue()->toBool();
        for (auto reg : circuit->manager().getRegisters(clock)) {
            if (reg.isPosTriggered() == posEdge) {
                const auto inNode = static_cast<DataNode *>(circuit->resolveNode(reg.getInput())); // NO-LINT: NOLINT(cppcoreguidelines-pro-type-static-cast-downcast)
                const auto outNode = static_cast<DataNode *>(circuit->resolveNode(reg.getOutput()));  // NO-LINT: NOLINT(cppcoreguidelines-pro-type-static-cast-downcast)
                if (needPropagate(inNode->getValue(), outNode->getValue())) {
                    incoming.emplace_back(outNode, inNode->getValue()->copyOf());
                }
            }
        }
        for (auto &[fst, snd] : incoming) {
            fst->setValue(snd);
            worklist.push(fst);
        }
        return !incoming.empty();
    }

    ClockNode * Simulator::pickClock() {
        if (clocks.empty()) {
            return nullptr;
        }
        if (clocks.size() == 1) {
            return clocks[0];
        }
        return clocks[supplier.nextInt(static_cast<int>(clocks.size()))];
    }

    std::vector<BitVector *> Simulator::evaluate(const std::string &id,
                                                 const std::vector<const BitVector *> &input) {
        static std::unordered_map<std::string, bool (BitVector::*)(const BitVector&) const> RELATIONAL = {
            {"SLT", &BitVector::SLT},
            {"SLE", &BitVector::SLE},
            {"SGT", &BitVector::SGT},
            {"SGE", &BitVector::SGE},
            {"ULT", &BitVector::ULT},
            {"ULE", &BitVector::ULE},
            {"UGT", &BitVector::UGT},
            {"UGE", &BitVector::UGE},
            {"EQ", &BitVector::operator==},
            {"NE", &BitVector::operator!=}
        };

        static std::unordered_map<std::string, BitVector *(BitVector::*)(const BitVector&) const> BINARY = {
            {"CONCAT", &BitVector::CONCAT},
            {"ADD", &BitVector::ADD},
            {"SUB", &BitVector::SUB},
            {"MUL", &BitVector::MUL},
            {"DIV", &BitVector::DIV},
            {"AND", &BitVector::AND},
            {"XOR", &BitVector::XOR},
            {"OR", &BitVector::OR},
            {"NAND", &BitVector::NAND},
            {"XNOR", &BitVector::XNOR},
            {"NOR", &BitVector::NOR},
        };

        static std::unordered_map<std::string, BitVector *(BitVector::*)(unsigned long) const> SHIFT = {
            {"SHL", &BitVector::SHL},
            {"ASHR", &BitVector::ASHR},
            {"LSHR", &BitVector::LSHR}
        };

        if (RELATIONAL.contains(id)) {
            if (input.size() != 2) {
                throw std::runtime_error("Invalid input size");
            }
            return {BinaryBitVector::fromBool((input[0]->*RELATIONAL[id])(*input[1]))};
        }
        if (BINARY.contains(id)) {
            BitVector *result = input[0]->copyOf();
            for (int i = 1; i != input.size(); ++i) {
                BitVector *value = (result->*BINARY[id])(*input[i]);
                delete result;
                result = value;
            }
            return {result};
        }
        if (SHIFT.contains(id)) {
            if (input.size() != 2) {
                throw std::runtime_error("Invalid input size");
            }
            return {(input[0]->*SHIFT[id])(input[1]->toUL())};
        }
        if (id == "NOT") {
            return {input[0]->NOT()};
        }
        if (id == "CAST") {
            std::vector<BitVector *> result;
            for (const auto value : input) {
                result.push_back(value->copyOf());
            }
            return result;
        }
        throw std::runtime_error("TODO: Not Implemented");
    }

    void Simulator::setInput(const std::string &id, BitVector *value) {
        const auto ptr = circuit->resolveInput(id);
        if (auto *data = dynamic_cast<DataNode *>(ptr); data != nullptr) {
            data->setValue(value);
            worklist.push(ptr);
            return;
        }
        throw std::runtime_error("Invalid input");
    }

    BitVector const *Simulator::getOutput(const std::string &id) const {
        const auto ptr = circuit->resolveOutput(id);
        if (const auto *data = dynamic_cast<DataNode *>(ptr); data != nullptr) {
            return data->getValue();
        }
        throw std::runtime_error("Invalid output");
    }

    Simulator::Simulator(Circuit *circuit, const bool DO_VERIFICATION, const bool RANDOM_INIT)
        : DO_VERIFICATION(DO_VERIFICATION), circuit(circuit), supplier(RANDOM_INIT) {
        circuit->topologicalSort();
        for (const auto node: circuit->getAllNodes()) {
            if (auto *wire = dynamic_cast<WireNode *>(node); wire != nullptr) {
                wire->setValue(supplier.nextBitVector(wire->getWidth()));
            } else if (auto *clock = dynamic_cast<ClockNode *>(node); clock != nullptr) {
                clock->setValue(supplier.nextBit());
                clocks.push_back(clock);
            }
        }
    }

    Simulator::~Simulator() {
        delete circuit;
    }

    BitVector *ValueSupplier::nextBitVector(const int size) {
        if (RANDOM_INIT) {
            if (size <= sizeof(unsigned long) * 8) {
                return BinaryBitVector::fromUL(
                    std::uniform_int_distribution(0, 1 << size - 1)(mt));
            }
            boost::dynamic_bitset<> data(size);
            for (int i = 0; i != size; ++i) {
                if (bitGen(mt)) {
                    data.set(i);
                }
            }
            return new BinaryBitVector(data);
        }
        return new BinaryBitVector(boost::dynamic_bitset<>(size, 0));
    }

    BitVector *ValueSupplier::nextBit() {
        if (RANDOM_INIT) {
            return BinaryBitVector::fromBool(bitGen(mt));
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
