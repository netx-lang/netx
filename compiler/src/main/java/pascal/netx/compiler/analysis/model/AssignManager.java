package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.circuit.Utils;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.variable.Composition;
import pascal.netx.compiler.circuit.variable.Data;
import pascal.netx.compiler.circuit.variable.Mutable;
import pascal.netx.compiler.circuit.variable.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AssignManager {
    private final ComponentScope scope;
    private final List<Mutable> defCount = new ArrayList<>();
    private final List<Mutable> useCount = new ArrayList<>();

    public AssignManager(ComponentScope scope) {
        this.scope = scope;
    }

    public void add(Variable lhs, Variable rhs) {
        use(rhs);
        define(lhs);
        if (lhs instanceof Composition lhsComposite && rhs instanceof Composition rhsComposite) {
            List<Data> lhsFlatten = lhsComposite.flatten(), rhsFlatten = rhsComposite.flatten();
            // TODO: add type checking
            int size = Utils.requireEqSize(lhsFlatten, rhsFlatten, "TODO");
            for (int i = 0; i < size; i++) {
                define(lhsFlatten.get(i));
                use(rhsFlatten.get(i));
                scope.getAliases().add(new ComponentScope.Alias(lhsFlatten.get(i), rhsFlatten.get(i)));
            }
        } else {
            scope.getAliases().add(new ComponentScope.Alias(lhs, rhs));
        }
    }

    private void define(Variable inst) {
        if (needUpdate(inst)) {
            defCount.add((Mutable) inst);
        }
    }

    private void use(Variable inst) {
        if (needUpdate(inst)) {
            useCount.add((Mutable) inst);
        }
    }

    public List<Mutable> getDefCount() {
        return Collections.unmodifiableList(defCount);
    }

    public List<Mutable> getUseCount() {
        return Collections.unmodifiableList(useCount);
    }

    private boolean needUpdate(Variable inst) {
        if (inst instanceof Mutable mutable) {
            return mutable.getParent() == scope;
        }
        return false;
    }
}
