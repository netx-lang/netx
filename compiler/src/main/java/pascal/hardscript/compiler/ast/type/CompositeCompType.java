package pascal.hardscript.compiler.ast.type;

import pascal.hardscript.compiler.ast.Identifier;

import java.util.List;

public record CompositeCompType(Identifier head, List<CompTypeNode> arguments) implements CompTypeNode {

    public static CompositeCompType of(Identifier head, List<CompTypeNode> arguments) {
        return new CompositeCompType(head, arguments);
    }
}
