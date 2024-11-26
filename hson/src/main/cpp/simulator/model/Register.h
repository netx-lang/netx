#pragma once
#include <unordered_map>
#include <vector>

namespace hson {
    class Register {
    public:
        Register(int input, int output, bool isPosTriggered);

    private:
        int input, output;

        bool isPosTriggered;
    };

    class RegisterManager {
        std::unordered_map<int, std::vector<Register> > registers;

        class Iterator {
            using mapIter = std::unordered_map<int, std::vector<Register>>::const_iterator;
            using vectorIter = std::vector<Register>::const_iterator;

            mapIter outer_it, outer_end;
            vectorIter inner_it;

            void advanceToValid();

        public:
            Iterator(mapIter outer_it, mapIter outer_end);

            const Register& operator*() const;

            Iterator& operator++();

            bool operator!=(const Iterator& other) const;
        };

    public:
        void addRegister(int clock, const Register &reg);

        [[nodiscard]] Iterator begin() const;

        [[nodiscard]] Iterator end() const;

        RegisterManager() = default;
    };
} // hson
