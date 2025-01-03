package pascal.netx.compiler.model.instance.selector;

public sealed interface Selector permits PartSelector, BitSelector {
    int getWidth();

    Selector refine(Selector selector);
}
