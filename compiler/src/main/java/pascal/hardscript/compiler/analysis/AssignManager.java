package pascal.hardscript.compiler.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.elaborator.Types;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AssignManager {
    private static final Logger log = LoggerFactory.getLogger(AssignManager.class);
    private final ComponentScope scope;
    private final List<MutableInstance> defCount = new ArrayList<>();
    private final List<MutableInstance> useCount = new ArrayList<>();

    public AssignManager(ComponentScope scope) {
        this.scope = scope;
    }

    public void assign(SourceLoc sourceLoc, Instance lhs, Instance rhs) {
        use(sourceLoc, rhs);
        define(sourceLoc, lhs);
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

    private void define(SourceLoc sourceLoc, Instance inst) {
        if (needUpdate(sourceLoc, inst)) {
            defCount.add((MutableInstance) inst);
        }
    }

    private void use(SourceLoc sourceLoc, Instance inst) {
        if (needUpdate(sourceLoc, inst)) {
            useCount.add((MutableInstance) inst);
        }
    }

    public List<MutableInstance> getDefCount() {
        return Collections.unmodifiableList(defCount);
    }

    public List<MutableInstance> getUseCount() {
        return Collections.unmodifiableList(useCount);
    }

    private boolean needUpdate(SourceLoc sourceLoc, Instance inst) {
        if (inst instanceof MutableInstance mutable) {
            return switch (mutable) {
                case Data data -> data.getParent() == scope;
                case DataBundle bundle -> bundle.getParent() == scope;
                case DataAccess access -> access.base().getParent() == scope;
                case Array _ -> {
                    log.error("{}: Array assignment is not supported", sourceLoc);
                    throw new CompileCancellationException();
                }
            };
        }
        return false;
    }
}
