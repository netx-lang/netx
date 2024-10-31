package pascal.hardscript.compiler.model.data.selector;

public sealed interface Selector permits PartSelector, BitSelector {
    int getWidth();
}
