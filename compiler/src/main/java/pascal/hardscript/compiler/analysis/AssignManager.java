package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.elaborator.Types;
import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataComposition;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.MutableInstance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AssignManager {
    private final ComponentScope scope;
    private final List<MutableInstance> defCount = new ArrayList<>();
    private final List<MutableInstance> useCount = new ArrayList<>();

    public AssignManager(ComponentScope scope) {
        this.scope = scope;
    }

    public void assign(SourceLoc sourceLoc, Instance lhs, Instance rhs) {
        use(rhs);
        define(lhs);
        if (lhs instanceof DataComposition lhsComposite && rhs instanceof DataComposition rhsComposite) {
            List<Data> lhsFlatten = lhsComposite.flatten(), rhsFlatten = rhsComposite.flatten();
            int size = Types.requiresMatchedSize(sourceLoc, lhsFlatten, rhsFlatten);
            for (int i = 0; i < size; i++) {
                define(lhsFlatten.get(i));
                use(rhsFlatten.get(i));
                scope.getAliases().add(Scope.Alias.of(rhsFlatten.get(i), lhsFlatten.get(i)));
            }
        } else {
            scope.getAliases().add(Scope.Alias.of(rhs, lhs));
        }
    }

    private void define(Instance inst) {
        if (needUpdate(inst)) {
            defCount.add((MutableInstance) inst);
        }
    }

    private void use(Instance inst) {
        if (needUpdate(inst)) {
            useCount.add((MutableInstance) inst);
        }
    }

    public List<MutableInstance> getDefCount() {
        return Collections.unmodifiableList(defCount);
    }

    public List<MutableInstance> getUseCount() {
        return Collections.unmodifiableList(useCount);
    }

    private boolean needUpdate(Instance inst) {
        if (inst instanceof MutableInstance mutable) {
            return mutable.getParent() == scope;
        }
        return false;
    }
}
