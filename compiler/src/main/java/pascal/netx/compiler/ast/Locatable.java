package pascal.netx.compiler.ast;

import pascal.netx.util.lang.SourceLoc;

public interface Locatable {
    SourceLoc getSourceLoc();
}
