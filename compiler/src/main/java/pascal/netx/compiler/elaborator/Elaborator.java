package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.DesignNode;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.component.ComponentAtom;
import pascal.netx.compiler.model.component.Signature;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);

    private final DesignNode node;
    private final ModuleScope module;
    private final Configuration config;

    public Elaborator(DesignNode node, Configuration config) {
        this.node = node;
        this.config = config;
        this.module = ModuleScope.newModule();
    }

    private static void setDesignSignature(ComponentScope topScope, ComponentNode topNode) {
        var atom = ComponentAtom.of(topNode, topNode.getIdentifier());
        topScope.setSignature(Signature.of(atom, topScope.getInputs(), topScope.getOutputs()));
    }

    public ModuleScope elaborate() {
        addModuleInformation();
        var topNode = findTopNode();
        var topScope = module.instantiateTop(topNode.getIdentifier());
        new ScopeElaborator(config, topScope).build(topNode);
        if (!config.fullyExpand()) {
            setDesignSignature(topScope, topNode);
        }
        return module;
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

    private void addModuleInformation() {
        for (var element : node.getElements()) {
            switch (element) {
                case ComponentNode component -> addComponent(component);
                case ImportNode _ -> {
                    // TODO:
                }
            }
        }
        module.resetDataIndexer();
    }

    private void addComponent(ComponentNode node) {
        switch (node.getKind()) {
            case COMPONENT -> module.addComponent(node);
            case INTERFACE -> module.addInterface(node);
        }
    }
}
