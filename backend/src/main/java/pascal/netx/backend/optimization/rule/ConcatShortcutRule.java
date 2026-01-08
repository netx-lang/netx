package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.ReadSelect;
import pascal.netx.backend.optimization.model.Vertex;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public class ConcatShortcutRule extends UniRule {
    public ConcatShortcutRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        boolean changed = false;
        if (vertex instanceof ReadSelect select) {
            var input = graph.inputsOf(select).getFirst();
            if (input.contains(Component.class, hasLabel(Component.Label.CONCAT))) {
                for (var concat : input.filter(Component.class, hasLabel(Component.Label.CONCAT))) {
                    int offset = 0;
                    for (var element : graph.inputsOf(concat).reversed()) {
                        if (offset == select.selector().lsb()) {
                            if (element.width() == select.selector().width()) {
                                Vertex v = element.getAny();
                                graph.manager().replace(select, v);
                                markSeen(vertex);
                                changed = true;
                            }
                            break;
                        }
                        offset += element.width();
                    }
                }
            }
        }

        return changed;
    }
}
