package pascal.netx.compiler.elaborator;

import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.ast.element.ComponentNode;
import pascal.netx.compiler.ast.element.EnumNode;
import pascal.netx.compiler.ast.element.ImportNode;
import pascal.netx.compiler.ast.element.InterfaceNode;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.model.Signature;
import pascal.netx.compiler.model.expr.ComponentRef;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.RepoScope;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.compiler.model.scope.binding.OrderedArgBindList;
import pascal.netx.util.lang.Identifier;

import static pascal.netx.compiler.model.Utils.requiresMatchedSize;

public final class PackageElaborator {
    private final PackageNode node;
    private final PackageScope pack;
    private final Configuration config;

    public PackageElaborator(RepoScope parent, PackageNode node, Configuration config) {
        this.node = node;
        this.config = config;
        this.pack = PackageScope.newPackage(parent);
    }

    private void setSignature(ComponentScope topScope, ComponentNode topNode) {
        var atom = ComponentRef.of(topNode, topNode.getIdentifier());
        atom.setArgList(config.arguments());
        topScope.setSignature(Signature.of(atom, topScope.getInputs(), topScope.getOutputs()));
    }

    public PackageScope elaborate() {
        Identifier name = node.getPackageInfo().getIdentifier();
        Logger.startPackage(name, node.getSourceLoc());
        initializePackageDefs();
        var topNode = findTopNode();
        var topScope = pack.instantiateTop(topNode.getIdentifier());
        bindConfigParameters(topNode, topScope);
        new ComponentElaborator(config, topScope).build(topNode);
        setSignature(topScope, topNode);
        Logger.finishPackage();
        return pack;
    }

    private void bindConfigParameters(ComponentNode topNode, ComponentScope topScope) {
        if (config.arguments() != null) {
            switch (config.arguments()) {
                case OrderedArgBindList(var args) -> {
                    int size = requiresMatchedSize(topNode.getSourceLoc(), topNode.getAttributes(), args);
                    for (int i = 0; i < size; i++) {
                        topScope.bind(topNode.getAttributes().get(i), args.get(i));
                    }
                }
                case MultiBinding binding -> binding.forEach(
                        entry -> topScope.bind(entry.getKey(), entry.getValue()));
            }
        }
    }

    private ComponentNode findTopNode() {
        ComponentNode result = null;
        for (var element : node.getElements()) {
            if (element instanceof ComponentNode component) {
                if (config.topName().equals(component.getIdentifier())) {
                    return component;
                } else if (result == null) {
                    result = component;
                }
            }
        }
        Logger.warn("No component named '{}' found, using the first component as top", config.topName().name());
        return result;
    }

    private void initializePackageDefs() {
        for (var element : node.getElements()) {
            switch (element) {
                case EnumNode _enum -> pack.defs().addEnum(_enum);
                case ImportNode _import -> pack.defs().addImport(_import);
                case ComponentNode component -> pack.defs().addComponent(component);
                case InterfaceNode _interface -> pack.defs().addInterface(_interface);
            }
        }
    }
}
