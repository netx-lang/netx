package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ModuleInfo(List<Identifier> modules) {

    private static final List<String> builtin = List.of(
            "NOT", "AND", "OR", "XOR", "TO"
    );

    public ModuleInfo(List<Identifier> modules) {
        this.modules = new ArrayList<>(modules);
        builtin.stream().map(Identifier::of).forEach(this.modules::add);
    }

    public boolean contains(Identifier identifier) {
        return modules.contains(identifier);
    }

    public static ModuleInfo of(List<Identifier> modules) {
        return new ModuleInfo(modules);
    }
}
