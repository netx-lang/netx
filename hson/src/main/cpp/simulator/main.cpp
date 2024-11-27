#include <iostream>

#include "Frontend.h"
#include "Simulator.h"

using namespace hson;

std::vector<std::string> split(const std::string& s, const std::string &delimiter) {
    size_t pos_start = 0, pos_end;
    const size_t delim_len = delimiter.length();
    std::vector<std::string> res;

    while ((pos_end = s.find(delimiter, pos_start)) != std::string::npos) {
        std::string token = s.substr(pos_start, pos_end - pos_start);
        pos_start = pos_end + delim_len;
        res.push_back (token);
    }

    res.push_back (s.substr (pos_start));
    return res;
}

int main(int argc, char *argv[]) {
    const auto frontend = Frontend::loadString(argv[1]);
    const auto circuit = frontend->parse();
    const auto simulator = new Simulator(circuit);

    std::string input;
    while (true) {
        std::getline(std::cin, input);
        if (input == "exit") {
            break;
        }

        auto command = split(input, " ");
        try {
            if (command[0] == "run") {
                auto start = std::chrono::high_resolution_clock::now();
                simulator->run();
                auto end = std::chrono::high_resolution_clock::now();
                auto ms_int = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
                std::cout << "execution finished in " << ms_int.count() << " ms" << std::endl;
            } else if (command[0] == "set") {
                const auto value = BinaryBitVector::parse(command[2]);
                simulator->setInput(command[1], value);
                std::cout << "input " + command[1] + " set to " + value->print() << std::endl;
            } else if (command[0] == "get") {
                const auto result = simulator->getOutput(command[1]);
                std::cout << result->print() << std::endl;
            } else {
                std::cout << "Invalid command" << std::endl;
            }
        } catch (const std::exception &e) {
            std::cout << e.what() << std::endl;
        }
    }
    return 0;
}
