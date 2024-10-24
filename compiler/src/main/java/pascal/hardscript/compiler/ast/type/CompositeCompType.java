package pascal.hardscript.compiler.ast.type;

import pascal.hardscript.compiler.ast.Symbol;

import java.util.List;

public record CompositeCompType(Symbol head, List<CompTypeNode> arguments) implements CompTypeNode {

    public static CompositeCompType of(Symbol head, List<CompTypeNode> arguments) {
        return new CompositeCompType(head, arguments);
    }
}
