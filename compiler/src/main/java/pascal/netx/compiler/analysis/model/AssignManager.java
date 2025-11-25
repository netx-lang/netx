package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.circuit.Utils;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.signal.Composition;
import pascal.netx.compiler.circuit.signal.Data;
import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.Wire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AssignManager {
    private final ComponentScope scope;
    private final List<Data> defCount = new ArrayList<>();
    private final List<Data> useCount = new ArrayList<>();

    public AssignManager(ComponentScope scope) {
        this.scope = scope;
    }

    public void add(Signal lhs, Signal rhs) {
        use(rhs);
        define(lhs);
        if (lhs instanceof Composition lhsComposite && rhs instanceof Composition rhsComposite) {
            List<Wire> lhsFlatten = lhsComposite.flatten(), rhsFlatten = rhsComposite.flatten();
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

    private void define(Signal inst) {
        if (needUpdate(inst)) {
            defCount.add((Data) inst);
        }
    }

    private void use(Signal inst) {
        if (needUpdate(inst)) {
            useCount.add((Data) inst);
        }
    }

    public List<Data> getDefCount() {
        return Collections.unmodifiableList(defCount);
    }

    public List<Data> getUseCount() {
        return Collections.unmodifiableList(useCount);
    }

    private boolean needUpdate(Signal inst) {
        if (inst instanceof Data data) {
            return data.getParent() == scope;
        }
        return false;
    }
}
