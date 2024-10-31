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
import pascal.hardscript.compiler.ast.expr.IdentifierSymbol;
import pascal.hardscript.compiler.ast.expr.InterpolatedSymbol;
import pascal.hardscript.compiler.ast.expr.ParamSymbolExpr;
import pascal.hardscript.compiler.ast.module.InstantiationNode;
import pascal.hardscript.compiler.ast.module.ModuleNode;

import java.util.*;

public final class HierarchyAnalysis extends ASTAnalysis<HierarchyAnalysis.AnalysisResult> {
    private static final Logger log = LoggerFactory.getLogger(HierarchyAnalysis.class);
    private final Graph<Identifier, DefaultEdge> dependencies = new SimpleDirectedGraph<>(DefaultEdge.class);
    private final Map<Identifier, ModuleNode> dict = new HashMap<>();

    public HierarchyAnalysis(DesignNode design) {
        super(design);
    }

    @Override
    public AnalysisResult analyze() {
        buildDependencies();
        return buildResult();
    }

    private void buildDependencies() {
        for (var module : design.getModules()) {
            var identifier = module.getIdentifier();
            dependencies.addVertex(identifier);
            dict.put(identifier, module);
        }

        design.getModules().stream().map(ModuleNode::getIdentifier).forEach(dependencies::addVertex);
        for (var module : design.getModules()) {
            var from = module.getIdentifier();
            var collector = new InstantiationCollector();
            module.walk(collector);
            for (var to : collector.references()) {
                dependencies.addEdge(from, to);
            }
        }
    }

    private AnalysisResult buildResult() {
        List<ModuleNode> topModules = new ArrayList<>();
        List<ModuleNode> topology = new ArrayList<>();
        var iterator = new TopologicalOrderIterator<>(dependencies);
        iterator.forEachRemaining(id -> {
            var module = dict.get(id);
            if (dependencies.inDegreeOf(id) == 0) {
                topModules.add(module);
            }
            topology.add(module);
        });
        return new AnalysisResult(topModules, topology);
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
        public void enter(ParamSymbolExpr node) {

        }

        @Override
        public void enter(IdentifierSymbol node) {
            var id = node.getIdentifier();
            if (dependencies.containsVertex(id)) {
                references.add(id);
            }
        }

        @Override
        public void enter(InterpolatedSymbol node) {
            log.error("{}: Interpolated identifiers are not allowed in module instantiation: {}",
                    node.getSourceLoc(), node);
        }
    }

    public record AnalysisResult(List<ModuleNode> topModules, List<ModuleNode> topology) {

    }
}
