package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.analysis.HierarchyAnalysis;
import pascal.hardscript.compiler.ast.DesignNode;

public final class Elaborator {
    private final DesignNode node;

    public Elaborator(DesignNode node) {
        this.node = node;
    }

    public void elaborate() {
        var hierarchy = new HierarchyAnalysis(node).analyze();

    }
}
