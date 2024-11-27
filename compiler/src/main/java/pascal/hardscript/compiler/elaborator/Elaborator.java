package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.io.Configuration;
import pascal.hardscript.compiler.model.component.ComponentAtom;
import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);

    private final DesignNode node;
    private final GlobalScope design;
    private final Configuration config;

    public Elaborator(DesignNode node, Configuration config) {
        this.node = node;
        this.design = GlobalScope.newDesign(node);
        this.config = config;
    }

    public GlobalScope elaborate() {
        var topNode = findTopNode();
        var topScope = design.instantiateTop(topNode.getIdentifier());
        new ScopeElaborator(config, topScope).build(topNode);
        if (!config.fullyExpand()) {
            setDesignSignature(topScope, topNode);
        }
        return design;
    }

    private static void setDesignSignature(ComponentScope topScope, ComponentNode topNode) {
        var atom = ComponentAtom.of(topNode, topNode.getIdentifier());
        topScope.setSignature(Signature.of(atom, topScope.getInputs(), topScope.getOutputs()));
    }

    private ComponentNode findTopNode() {
        for (var component : node.getComponents()) {
            if (config.topName().equals(component.getIdentifier().name())) {
                return component;
            }
        }
        log.warn("No component named '{}' found, using the first component as top", config.topName());
        return node.getComponents().getFirst();
    }

}
