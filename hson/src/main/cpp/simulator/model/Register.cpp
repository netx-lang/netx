#include "Register.h"

#include "Node.h"

namespace hson {
    Register::Register(const int input, const int output, const bool isPosTriggered)
        : input(input), output(output), posTriggered(isPosTriggered) {
    }

    int Register::getInput() const {
        return input;
    }

    int Register::getOutput() const {
        return output;
    }

    bool Register::isPosTriggered() const {
        return posTriggered;
    }

    void RegisterManager::Iterator::advanceToValid() {
        while (outer_it != outer_end && inner_it == outer_it->second.end()) {
            ++outer_it;
            if (outer_it != outer_end) {
                inner_it = outer_it->second.begin();
            }
        }
    }

    RegisterManager::Iterator::Iterator(const mapIter outer_it, const mapIter outer_end): outer_it(outer_it), outer_end(outer_end) {
        if (outer_it != outer_end) {
            inner_it = outer_it->second.begin();
            advanceToValid();
        }
    }

    const Register & RegisterManager::Iterator::operator*() const {
        return *inner_it;
    }

    RegisterManager::Iterator & RegisterManager::Iterator::operator++() {
        ++inner_it;
        advanceToValid();
        return *this;
    }

    bool RegisterManager::Iterator::operator!=(const Iterator &other) const {
        return outer_it != other.outer_it ||
               (outer_it != outer_end && inner_it != other.inner_it);
    }

    void RegisterManager::addRegister(const int clock, const Register &reg) {
        registers[clock].push_back(reg);
    }

    void RegisterManager::addClock(const int clock) {
        registers[clock] = std::vector<Register>{};
    }

    const std::vector<Register> & RegisterManager::getRegisters(const ClockNode *clock) const {
        return registers.at(clock->getIndex());
    }

    RegisterManager::Iterator RegisterManager::begin() const {
        return {registers.begin(), registers.end()};
    }

    RegisterManager::Iterator RegisterManager::end() const {
        return {registers.end(), registers.end()};
    }
} // hson