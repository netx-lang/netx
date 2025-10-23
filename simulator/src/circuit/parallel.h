//
// Created by czm233 on 7/11/25.
//

#ifndef PARALLEL_H
#define PARALLEL_H
#include <string>
#include <ranges>

#include "circuit_parser.h"
#include "parallel/partition.h"

namespace nxon {
    template<typename PartitionedContext, partitioner_t auto Partitioner>
    struct partitioned_parse_context_common : parse_context {
        void init_partition() {
            partition_res = Partitioner(*this);
            partitioned = partitioned_circuit<PartitionedContext>(circuit, partition_res);
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

        void export_dot(std::ostream &out) const {
            constexpr const char *GRAPHVIZ_COLOR_LIST[] = {
                "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black",
                "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate",
                "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod",
                "darkgray", "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange",
                "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray",
                "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey",
                "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold",
                "goldenrod", "gray", "grey", "green", "greenyellow", "honeydew", "hotpink", "indianred", "indigo",
                "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral",
                "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon",
                "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow",
                "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid",
                "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise",
                "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy",
                "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen",
                "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue",
                "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen",
                "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen",
                "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke",
                "yellow", "yellowgreen"
            };
            constexpr size_t COLOR_COUNT = std::size(GRAPHVIZ_COLOR_LIST);

            const auto graph = impl::extract_graph(circuit);
            out << "digraph {\n";
            for (const auto [vert_begin, vert_end] = vertices(graph);
                 const auto v: std::ranges::subrange(vert_begin, vert_end)) {
                out << v;
                const auto id = graph[v];
                if (const auto iter = partition_res.belongs.find(id); iter != partition_res.belongs.end())
                    out << " [color=" << GRAPHVIZ_COLOR_LIST[iter->second % COLOR_COUNT] << ']';
                out << '\n';
            }
            for (const auto [edge_begin, edge_end] = edges(graph);
                 const auto edge: std::ranges::subrange(edge_begin, edge_end))
                out << edge.m_source << " -> " << edge.m_target << '\n';
            out << "}\n";
        }

    private:
        partition_result partition_res;
        std::optional<partitioned_circuit<PartitionedContext> > partitioned;
    };

    template<partitioner_t auto Partitioner = kahip_partitioner>
    using partitioned_parse_context = partitioned_parse_context_common<impl::partitioned_circuit_context, Partitioner>;
    template<partitioner_t auto Partitioner = kahip_partitioner>
    using parallel_parse_context = partitioned_parse_context_common<impl::parallel_partitioned_circuit_context,
        Partitioner>;
}

#endif //PARALLEL_H
