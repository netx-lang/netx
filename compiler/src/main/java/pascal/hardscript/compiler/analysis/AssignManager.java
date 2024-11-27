package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.instance.Data;
import pascal.hardscript.compiler.model.instance.DataAccess;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.MutableInstance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;

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

    public void assign(Instance lhs, Instance rhs) {
        use(rhs);
        define(lhs);
        scope.getAliases().add(Scope.Alias.of(rhs, lhs));
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
                case DataAccess access -> access.base().getParent() == scope;
            };
        }
        return false;
    }
}
