//
// Created by czm233 on 7/11/25.
//

#ifndef PARALLEL_H
#define PARALLEL_H
#include "circuit_parser.h"
#include "partition.h"

namespace nxon {
    struct parallel_parse_context : parse_context {
        void init_parallel() {
            partitioned = partitioned_circuit(circuit);
        }

        [[nodiscard]] value_t get(const id_t id) const {
            return partitioned->get_value(id);
        }

        void flip(const id_t id) {
            partitioned->flip(id);
        }

        void flip_batch(const id_set &ids) {
            partitioned->flip_batch(ids);
        }

        void update(const id_t id, const value_t &value) {
            partitioned->update(id, value);
        }

        void update_batch(const value_batch &batch) {
            partitioned->update_batch(batch);
        }

        void broadcast_flip(const id_t id) {
            partitioned->broadcast_flip(id);
        }

        void broadcast_flip_batch(const id_set &ids) {
            partitioned->broadcast_flip_batch(ids);
        }

        void broadcast(const id_t id, const value_t &value) {
            partitioned->broadcast(id, value);
        }

        void broadcast_batch(const value_batch &batch) {
            partitioned->broadcast_batch(batch);
        }

        void broadcast_flip_by_name(const std::string &name) {
            broadcast_flip(name_map.at(name));
        }

        void broadcast_by_name(const std::string &name, const value_t &value) {
            broadcast(name_map.at(name), value);
        }

        void run_to_fixed() {
            partitioned->run_to_fixed();
        }

    private:
        std::optional<partitioned_circuit> partitioned;
    };
}

#endif //PARALLEL_H
