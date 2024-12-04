package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.elaborator.Types;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignManager {
    private final ComponentScope scope;
    private final List<MutableInstance> defCount = new ArrayList<>();
    private final List<MutableInstance> useCount = new ArrayList<>();

    public AssignManager(ComponentScope scope) {
        this.scope = scope;
    }

    public void assign(SourceLoc sourceLoc, Instance lhs, Instance rhs) {
        use(rhs);
        define(lhs);
        if (lhs instanceof DataBundle lhsBundle && rhs instanceof DataBundle rhsBundle) {
            List<Data> lhsFlatten = lhsBundle.flatten(), rhsFlatten = rhsBundle.flatten();
            int size = Types.requiresMatchedSize(sourceLoc, lhsFlatten, rhsFlatten);
            for (int i = 0; i < size; i++) {
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
            return switch (mutable) {
                case Data data -> data.getParent() == scope;
                case DataBundle bundle -> bundle.getParent() == scope;
                case DataAccess access -> access.base().getParent() == scope;
            };
        }
        return false;
    }
}
