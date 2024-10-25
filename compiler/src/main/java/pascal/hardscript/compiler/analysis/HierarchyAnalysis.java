package pascal.hardscript.compiler.analysis;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.Interpolated;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;
import pascal.hardscript.compiler.ast.module.InstantiationNode;
import pascal.hardscript.compiler.ast.module.ModuleNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;

import java.util.HashSet;
import java.util.Set;

public final class HierarchyAnalysis extends ASTAnalysis {

    private static final Logger log = LoggerFactory.getLogger(HierarchyAnalysis.class);

    private final Graph<Identifier, DefaultEdge> dependencies = new SimpleDirectedGraph<>(DefaultEdge.class);

    public HierarchyAnalysis(DesignNode design) {
        super(design);
    }

    @Override
    public void analyze() {
        var collector = new InstantiationCollector();
        design.getModules().stream().map(ModuleNode::getIdentifier).forEach(dependencies::addVertex);
        for (var module : design.getModules()) {
            var from = module.getIdentifier();
            module.walk(collector);
            for (var to : collector.references()) {
                dependencies.addEdge(from, to);
            }
        }
        var iterator = new TopologicalOrderIterator<>(dependencies);
        while (iterator.hasNext()) {
            log.info("{}", iterator.next());
        }
    }

    private class InstantiationCollector implements ASTListener {
        private final ModuleCollector moduleCollector = new ModuleCollector();

        public Set<Identifier> references() {
            return moduleCollector.references();
        }

        @Override
        public void enter(InstantiationNode node) {
            node.getType().walk(moduleCollector);
        }
    }

    private class ModuleCollector implements ASTListener {
        private final Set<Identifier> references = new HashSet<>();

        public Set<Identifier> references() {
            return references;
        }

        @Override
        public void enter(SymbolExpr node) {
            switch (node.getSymbol()) {
                case Identifier id -> {
                    if (dependencies.containsVertex(id)) {
                        references.add(id);
                    }
                }
                case Interpolated interpolated -> {
                    log.error("Interpolated identifiers are not allowed in module instantiation: {}", interpolated);
                    throw new CompileCancellationException();
                }
            }
        }
    }
}
