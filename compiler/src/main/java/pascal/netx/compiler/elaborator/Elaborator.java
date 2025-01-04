package pascal.netx.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.ast.DesignNode;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.component.ComponentAtom;
import pascal.netx.compiler.model.component.Signature;
import pascal.netx.compiler.model.instance.*;
import pascal.netx.compiler.model.instance.type.PolyType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;
import pascal.netx.util.lang.Identifier;

import java.util.LinkedHashMap;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);

    private final DesignNode node;
    private final ModuleScope module;
    private final Configuration config;

    public Elaborator(DesignNode node, Configuration config) {
        this.node = node;
        this.module = ModuleScope.newModule();
        this.config = config;
    }

    private static void setDesignSignature(ComponentScope topScope, ComponentNode topNode) {
        var atom = ComponentAtom.of(topNode, topNode.getIdentifier());
        topScope.setSignature(Signature.of(atom, topScope.getInputs(), topScope.getOutputs()));
    }

    public ModuleScope elaborate() {
        addGlobalInformation();
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

    private void addGlobalInformation() {
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
            case INTERFACE -> {
                module.addComponent(node);
                module.addInterface(node.getIdentifier(), elaborateInterface(node));
            }
        }
    }

    private DataBundle.Factory elaborateInterface(ComponentNode node) {
        var scope = ComponentScope.newInterface(module, node.getIdentifier());
        new ScopeElaborator(config, scope).build(node);

        LinkedHashMap<Identifier, MutableInstance.Factory<?>> factories = new LinkedHashMap<>();
        for (var inst : scope.getOutputs()) {
            if (!(inst instanceof MutableInstance mutable)) {
                log.error("{}: Expected mutable instance but got {}", node.getSourceLoc(), inst);
                throw new CompileCancellationException();
            }
            var factory = getFactoryFrom(node, mutable);
            factories.put(mutable.getIdentifier(), factory);
        }
        return new DataBundle.Factory(scope.getIdentifier(), module, factories);
    }

    private MutableInstance.Factory<?> getFactoryFrom(ComponentNode node, MutableInstance mutable) {
        return switch (mutable) {
            case Clock _ -> new Clock.Factory(module);
            case Wire wire -> {
                if (wire.getType() instanceof PolyType) {
                    log.error("{}: Interface should not contain auto-typed instance: {}", node.getSourceLoc(), wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), module);
            }
            case DataBundle bundle -> module.getInterface(bundle.getType().getId());
            case Array array -> {
                var elementFactory = getFactoryFrom(node, array.getElements().getFirst());
                yield new Array.Factory(module, array.getElements().size(), elementFactory);
            }
            case DataAccess _ -> {
                log.error("{}: Interface should not contain data access: {}", node.getSourceLoc(), mutable);
                throw new CompileCancellationException();
            }
        };
    }
}
