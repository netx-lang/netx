package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.LinkedList;
import java.util.List;

public final class ScopeInliner extends ModelAnalysis {
    public ScopeInliner(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        inlineScope(design.getTop());
    }

    private void inlineScope(Scope scope) {
        List<Scope> inlinedChildren = new LinkedList<>();
        var binding = scope.getBinding();
        var aliases = scope.getAliases();

        for (var child : scope.getChildren()) {
            inlineScope(child);
            if (child instanceof ComponentScope) {
                inlinedChildren.add(child);
            } else {
                binding.add(child.getBinding());
                aliases.addAll(child.getAliases());
                inlinedChildren.addAll(child.getChildren());
                for (var grandChild : child.getChildren()) {
                    grandChild.setParent(scope);
                }
            }
        }
        scope.setChildren(inlinedChildren);
    }
}
