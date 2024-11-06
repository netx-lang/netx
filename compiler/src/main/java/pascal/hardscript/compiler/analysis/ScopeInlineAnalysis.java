package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.LinkedList;
import java.util.List;

public class ScopeInlineAnalysis extends ModelAnalysis {

    public ScopeInlineAnalysis(DesignScope design) {
        super(design);
    }

    @Override
    public void analyze() {
        design.getChildren().forEach(this::inline);
    }

    private void inline(Scope scope) {
        List<Scope> inlinedChildren = new LinkedList<>();
        var binding = scope.getBinding();

        for (var child : scope.getChildren()) {
            inline(child);
            if (child instanceof ComponentScope) {
                inlinedChildren.add(child);
            } else {
                binding = binding.merge(child.getBinding());
                inlinedChildren.addAll(child.getChildren());
                for (var grandChild : child.getChildren()) {
                    grandChild.setParent(scope);
                }
            }
        }
        scope.setBinding(binding);
        scope.setChildren(inlinedChildren);
    }
}
