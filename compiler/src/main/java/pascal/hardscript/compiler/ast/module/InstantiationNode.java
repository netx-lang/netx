package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.expr.list.ListExpr;

import java.util.ArrayList;
import java.util.List;

public record InstantiationNode(List<Entry> instances, ListExpr output) implements ElementNode {

    public static InstantiationNode of(List<InstRef> refs, List<ListExpr> ports) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < refs.size(); i++) {
            entries.add(new Entry(refs.get(i), ports.get(i)));
        }
        return new InstantiationNode(entries, ports.getLast());
    }

    private record Entry(InstRef ref, ListExpr input) {

    }
}
