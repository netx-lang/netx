package pascal.hardscript.compiler.parser;

import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.ModuleNode;
import pascal.hardscript.compiler.parser.HardScriptParser.DesignContext;
import pascal.hardscript.compiler.parser.HardScriptParser.ModuleContext;
import pascal.hardscript.util.exception.TodoException;

public final class HSParser {

    public static DesignNode parseDesign(DesignContext ctx) {
        return DesignNode.of(ctx.module().stream().map(HSParser::parseModule).toList());
    }

    public static ModuleNode parseModule(ModuleContext ctx) {
        throw new TodoException("parseModule");
    }
}
