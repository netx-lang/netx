//
// Created by czm233 on 12/11/24.
//
#include <iostream>
#include <fstream>
#include <memory>
#include <string>
#include <cli/cli.h>
#include <cli/clifilesession.h>

#include <boost/multiprecision/cpp_int.hpp>
#define NX_BACKEND boost::multiprecision::cpp_int
#include "simulation.h"

void launch_cli(auto &parsed) {
    auto rootMenu = std::make_unique<cli::Menu>("nxsim");
    rootMenu->Insert("iset", [&](std::ostream &out, const nxon::id_t id, const std::string &rep) {
        const auto val = nxon::value_t::parse(rep);
        parsed.iset(id, val);
        out << id << ": " << val << std::endl;
    }, "Set the value of the given wire (literal width must match wire)",
       {"wire id", "NetX literal: \"8'hAB\", \"4'b0011\", \"8'o77\", \"8'd10\", or 10 (32-bit)"});
    rootMenu->Insert("iget", [&](std::ostream &out, const nxon::id_t id) {
        out << id << ": " << parsed.iget(id) << std::endl;
    }, "Get the value of the given wire", {"wire id"});
    rootMenu->Insert("iflip", [&](std::ostream &out, const nxon::id_t id) {
        parsed.iflip(id);
        out << id << ": " << parsed.iget(id) << std::endl;
    }, "Flip the given wire", {"wire id"});

    rootMenu->Insert("set", [&](std::ostream &out, const std::string &name, const std::string &rep) {
        const auto val = nxon::value_t::parse(rep);
        parsed.set(name, val);
        out << name << ": " << val << std::endl;
    }, "Set the value of the given wire (literal width must match wire)",
       {"wire name", "NetX literal: \"8'hAB\", \"4'b0011\", \"8'o77\", \"8'd10\", or 10 (32-bit)"});
    rootMenu->Insert("get", [&](std::ostream &out, const std::string &name) {
        out << name << ": " << parsed.get(name) << std::endl;
    }, "Get the value of the given wire", {"wire name"});
    rootMenu->Insert("flip", [&](std::ostream &out, const std::string &name) {
        parsed.flip(name);
        out << name << ": " << parsed.get(name) << std::endl;
    }, "Flip the given wire", {"wire name"});

    rootMenu->Insert("fc", [&](std::ostream &out) {
        parsed.flip_input_clocks();
        out << "Flipped input clocks" << std::endl;
    }, "Flip input clocks");
    rootMenu->Insert("ac", [&](std::ostream &out) {
        parsed.advance_input_clocks();
        out << "Advanced input clocks" << std::endl;
    }, "Advance (flip twice) input clocks");

    cli::Cli cli(std::move(rootMenu));
    cli.StdExceptionHandler(
        [](std::ostream &out, const std::string &cmd, const std::exception &e) {
            out << "Caught exception: "
                    << e.what()
                    << " while handling command: "
                    << cmd
                    << "."
                    << std::endl;
        }
    );
    cli::CliFileSession input(cli);
    input.Start();
}

int main(const int argc, char *argv[]) {
    if (argc < 2 || argc > 3) {
        std::cout << "Usage: nxsim_cli <path to NXON file> (<directory for waveform dumping>)" << std::endl;
        return 1;
    }
    std::ifstream in{argv[1]};
    if (!in) {
        std::cerr << "Can't open file " << argv[1] << std::endl;
        return 2;
    }

    if (argc == 2) {
        auto parsed = nxon::parse_circuit(in);
        launch_cli(parsed);
    } else {
        nxon::dumping_parse_context parsed(argv[2]);
        nxon::parse_circuit(parsed, in);
        parsed.init_dump();
        launch_cli(parsed);
    }

    return 0;
}
