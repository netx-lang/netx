//
// Created by czm233 on 12/11/24.
//

#include <iostream>

#include "circuit/circuit.h"

int main() {
    hson::circuit my_circuit;
    my_circuit.add_rule(hson::rule_factory::and_rule(hson::data_factory::id_source(1), hson::data_factory::id_source(2), hson::data_factory::id_sink(3)));
    my_circuit.add_rule(hson::rule_factory::and_rule(hson::data_factory::id_source(4), hson::data_factory::id_source(5), hson::data_factory::id_sink(6)));
    my_circuit.add_rule(hson::rule_factory::or_rule(hson::data_factory::id_source(3), hson::data_factory::id_source(6), hson::data_factory::id_sink(7)));

    hson::value_storage values;
    for (size_t i = 1; i <= 7; ++i) values[i] = hson::value_t(1, 0ul);
    my_circuit.update_batch(values);

    std::cout << my_circuit.get_value(7).to_ulong() << '\n';

    my_circuit.update(1, hson::value_t{1, 1ul});
    std::cout << my_circuit.get_value(7).to_ulong() << '\n';

    my_circuit.update(2, hson::value_t{1, 1ul});
    std::cout << my_circuit.get_value(7).to_ulong() << '\n';
}
