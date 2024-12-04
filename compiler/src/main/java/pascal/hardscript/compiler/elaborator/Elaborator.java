package pascal.hardscript.compiler.elaborator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.component.BindingNode;
import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.io.Configuration;
import pascal.hardscript.compiler.model.component.ComponentAtom;
import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.util.lang.Identifier;

import java.util.LinkedHashMap;

public final class Elaborator {
    private static final Logger log = LoggerFactory.getLogger(Elaborator.class);

    private final DesignNode node;
    private final GlobalScope design;
    private final Configuration config;

    public Elaborator(DesignNode node, Configuration config) {
        this.node = node;
        this.design = GlobalScope.newDesign();
        this.config = config;
    }

    public GlobalScope elaborate() {
        addGlobalInformation();
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
        ComponentNode result = null;
        for (var element : node.getElements()) {
            var optional = element.asRight();
            if (optional.isPresent()) {
                var component = optional.get().value();
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
            element.match(this::addBinding, this::addComponent);
        }
        design.resetDataIndexer();
    }

    private void addBinding(BindingNode node) {
        var evaluator = design.getEvaluator();
        design.bind(evaluator.evaluateBinding(node));
    }

    private void addComponent(ComponentNode node) {
        switch (node.getKind()) {
            case COMPONENT -> design.addComponent(node);
            case INTERFACE -> {
                design.addComponent(node);
                design.addInterface(node.getIdentifier(), elaborateInterface(node));
            }
        }
    }

    private DataBundle.Factory elaborateInterface(ComponentNode node) {
        var scope = design.instantiateInterfaceScope(node.getIdentifier());
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
        return new DataBundle.Factory(scope.getIdentifier(), design, factories);
    }

    private MutableInstance.Factory<?> getFactoryFrom(ComponentNode node, MutableInstance mutable) {
        return switch (mutable) {
            case Clock _ -> new Clock.Factory(design);
            case Wire wire -> {
                if (wire.getType() instanceof PolyType) {
                    log.error("{}: Interface should not contain auto-typed instance: {}", node.getSourceLoc(), wire);
                    throw new CompileCancellationException();
                }
                yield new Wire.Factory(wire.getType(), design);
            }
            case DataBundle bundle -> design.getInterface(bundle.getType().getId());
            case Array array -> {
                var elementFactory = getFactoryFrom(node, array.getElements().getFirst());
                yield new Array.Factory(design, array.getElements().size(), elementFactory);
            }
            case DataAccess _ -> {
                log.error("{}: Interface should not contain data access: {}", node.getSourceLoc(), mutable);
                throw new CompileCancellationException();
            }
        };
    }
}
