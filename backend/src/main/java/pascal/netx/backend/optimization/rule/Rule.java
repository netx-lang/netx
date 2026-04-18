package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;

public interface Rule {
    boolean apply(Component vertex);
}

