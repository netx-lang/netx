package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.*;

public final class SelectPropagateRule extends UniRule {
    public SelectPropagateRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (!(vertex instanceof ReadSelect select)
                || select.selector().lsb() != 0) {
            return false;
        }

        int width = select.selector().width();
        boolean changed = false;
        var cls = graph.inputsOf(vertex).getFirst();
        for (var input : cls) {
            if (hasMarker(input, Marker.SelectProp) || input.outWidth() == width) {
                continue;
            }

            switch (input) {
                case Constant c -> {
                    var dual = graph.factory().constant(c.value().select(width - 1, 0));
                    graph.manager().union(vertex, dual);
                    mark(input, Marker.SelectProp);
                    changed = true;
                }
                case Register reg -> {
                    var inputs = graph.inputsOf(reg);
                    var dual = graph.factory().register(width, reg.isPosEdge());
                    var selected = graph.factory().readSelect(select.selector());
                    graph.addEdge(inputs.get(0), dual);
                    graph.addEdge(inputs.get(1), selected);
                    graph.addEdge(selected, dual);
                    graph.manager().union(vertex, dual);
                    mark(input, Marker.SelectProp);
                    changed = true;
                }
                case Component comp -> {
                    switch (comp.label()) {
                        case CONCAT -> {
                            assert cls.isSingleton();
                        }
                        case CAST, UCAST -> {
                            var furIn = graph.inputsOf(comp).getFirst();
                            if (furIn.width() == width) {
                                graph.manager().union(vertex, furIn.getAny());
                                mark(input, Marker.SelectProp);
                                changed = true;
                            } else {
                                var dual = graph.factory().component(comp.label(), width);
                                graph.addEdge(furIn, dual);
                                graph.manager().union(vertex, dual);
                                mark(input, Marker.SelectProp);
                                changed = true;
                            }
                        }
                        default -> {
                            var dual = graph.factory().component(comp.label(), width);
                            for (var furIn : graph.inputsOf(comp)) {
                                var selected = graph.factory().readSelect(select.selector());
                                graph.addEdge(furIn, selected);
                                graph.addEdge(selected, dual);
                            }
                            graph.manager().union(vertex, dual);
                            mark(input, Marker.SelectProp);
                            changed = true;
                        }
                    }
                }
                default -> changed = false;
            }
        }
        return changed;
    }
}
