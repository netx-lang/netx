#include <iostream>

#include "Frontend.h"
#include "Simulator.h"

namespace hson {
    class CommandLine {
        Simulator *simulator;

        static void ioFeedback(Circuit * const circuit) {
            for (const auto &input: circuit->getInputs()) {
                if (const auto *clock = dynamic_cast<ClockNode *>(input); clock != nullptr) {
                    std::cout << clock->id() << ' ';
                } else if (const auto *wire = dynamic_cast<WireNode *>(input); wire != nullptr) {
                    std::cout << wire->id() << ' ';
                }
            }
            for (const auto &output: circuit->getOutputs()) {
                if (const auto *clock = dynamic_cast<ClockNode *>(output); clock != nullptr) {
                    std::cout << clock->id() << ' ';
                } else if (const auto *wire = dynamic_cast<WireNode *>(output); wire != nullptr) {
                    std::cout << wire->id() << ' ';
                }
            }
            std::cout << std::endl;
        }

        static std::vector<std::string> split(const std::string& s) {
            size_t pos_start = 0, pos_end;
            std::vector<std::string> res;
            while ((pos_end = s.find(' ', pos_start)) != std::string::npos) {
                std::string token = s.substr(pos_start, pos_end - pos_start);
                pos_start = pos_end + 1;
                res.push_back (token);
            }
            res.push_back (s.substr (pos_start));
            return res;
        }

    public:
        explicit CommandLine(const std::string &literal) {
            const auto frontend = Frontend::loadString(literal);
            const auto circuit = frontend->parse();
            simulator = new Simulator(circuit);
            ioFeedback(circuit);
        }

        void call(const std::string &input) {
            static std::unordered_map<std::string, void (CommandLine::*)(const std::vector<std::string> &) const> menu = {
                {"run", &CommandLine::runCommand},
                {"set", &CommandLine::setCommand},
                {"get", &CommandLine::getCommand},
                {"exit", &CommandLine::exitCommand}
            };
            if (const auto command = split(input); !command.empty() && menu.contains(command[0])) {
                (this->*menu[command[0]])(command);
            } else {
                std::cout << "Invalid command" << std::endl;
            }
        }

        void runCommand(const std::vector<std::string> &args) const {
            if (args.size() != 1) {
                std::cout << "Invalid command" << std::endl;
                return;
            }
            const auto start = std::chrono::high_resolution_clock::now();
            simulator->run();
            const auto end = std::chrono::high_resolution_clock::now();
            const auto ms_int = std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
            std::cout << "execution finished in " << ms_int.count() << " ms" << std::endl;
        }

        void setCommand(const std::vector<std::string> &args) const {
            if (args.size() != 3) {
                std::cout << "Invalid command" << std::endl;
                return;
            }
            const auto value = BinaryBitVector::parse(args[2]);
            simulator->setInput(args[1], value);
            std::cout << "input " + args[1] + " set to " + value->print() << std::endl;
        }

        void getCommand(const std::vector<std::string> &args) const {
            if (args.size() != 2) {
                std::cout << "Invalid command" << std::endl;
                return;
            }
            const auto result = simulator->getOutput(args[1]);
            std::cout << result->print() << std::endl;
        }

        void exitCommand(const std::vector<std::string> &args) const {
            if (args.size() != 1) {
                std::cout << "Invalid command" << std::endl;
                return;
            }
            delete simulator;
        }
    };
}

int main(int argc, char *argv[]) {
    auto commandLind = hson::CommandLine(argv[1]);

    std::string input;
    while (true) {
        std::getline(std::cin, input);
        try {
            commandLind.call(input);
            if (input == "exit") {
                break;
            }
        } catch (const std::exception &e) {
            std::cout << e.what() << std::endl;
        }
    }
    return 0;
}