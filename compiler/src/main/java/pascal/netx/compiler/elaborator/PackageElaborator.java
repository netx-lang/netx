package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.EnumNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.Signature;
import pascal.netx.compiler.model.Utils;
import pascal.netx.compiler.model.expr.ComponentRef;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.ProjectScope;

import static pascal.netx.compiler.model.Utils.requiresMatchedSize;

public final class PackageElaborator {
    private static final Logger log = LoggerFactory.getLogger(PackageElaborator.class);

    private final PackageNode node;
    private final PackageScope pack;
    private final Configuration config;

    public PackageElaborator(ProjectScope parent, PackageNode node, Configuration config) {
        this.node = node;
        this.config = config;
        this.pack = PackageScope.newPackage(parent);
    }

    private static void setDesignSignature(ComponentScope topScope, ComponentNode topNode) {
        var atom = ComponentRef.of(topNode, topNode.getIdentifier());
        topScope.setSignature(Signature.of(atom, topScope.getInputs(), topScope.getOutputs()));
    }

    public PackageScope elaborate() {
        addPackageInformation();
        var topNode = findTopNode();
        var topScope = pack.instantiateTop(topNode.getIdentifier());
        bindConfigParameters(topNode, topScope);
        new ComponentElaborator(config, topScope).build(topNode);
        if (!config.fullyExpand()) {
            setDesignSignature(topScope, topNode);
        }
        return pack;
    }

    private void bindConfigParameters(ComponentNode topNode, ComponentScope topScope) {
        if (config.parameters() != null) {
            var iterator = config.parameters().iterator();
            requiresMatchedSize(topNode.getSourceLoc(), topNode.getAttributes(), config.parameters());
            for (var id : topNode.getAttributes()) {
                topScope.bind(id, iterator.next());
            }
        }
    }

    private ComponentNode findTopNode() {
        ComponentNode result = null;
        for (var element : node.getElements()) {
            if (element instanceof ComponentNode component) {
                if (config.topName().equals(component.getIdentifier().name())) {
                    return component;
                } else if (result == null) {
                    result = component;
                }
            }
        }
        log.warn("No component named '{}' found, using the first component as top", config.topName());
        return result;
    }

    private void addPackageInformation() {
        var compiler = Utils.compilerOf(pack);
        var indexer = compiler.getDataIndexer();
        compiler.setDataIndexer(NXCompiler.newIndexer());
        for (var element : node.getElements()) {
            switch (element) {
                case ComponentNode component -> addComponentInfo(component);
                case ImportNode import_ -> pack.addImport(import_);
                case EnumNode enum_ -> pack.addEnum(enum_);
            }
        }
        compiler.setDataIndexer(indexer);
    }

    private void addComponentInfo(ComponentNode node) {
        switch (node.getKind()) {
            case COMPONENT -> pack.addComponent(node);
            case INTERFACE -> pack.addInterface(node);
        }
    }
}
