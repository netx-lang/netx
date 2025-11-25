package pascal.netx.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.compiler.analysis.model.*;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.scope.RepoScope;
import pascal.netx.compiler.circuit.scope.binding.ArgBindList;
import pascal.netx.compiler.io.Configuration;
import pascal.netx.compiler.io.NxonDumper;
import pascal.netx.compiler.io.PackageManager;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The compiler is responsible for compiling a NetX repository
 */
public final class NXCompiler {
    private final RepoScope repo;
    private final Configuration config;
    private final PackageManager manager;

    /**
     * A supplier that generates unique data index.
     * During compilation, once a datum is instantiated,
     * it will be assigned with a unique index, which distinguishes it from other data in the repo.
     */
    private Supplier<Integer> dataIndexer;

    public NXCompiler(Configuration config, PackageManager manager) {
        this.config = config;
        this.manager = manager;
        this.repo = new RepoScope();
        repo.setManager(manager);
        repo.setCompiler(this);
        manager.setRepo(repo);
        this.dataIndexer = newIndexer();
    }

    public static NXCompiler forSinglePackage(PackageNode node, Configuration config) {
        var manager = PackageManager.ofEmpty();
        manager.addPackage(node);
        return new NXCompiler(config, manager);
    }

    public static Supplier<Integer> newIndexer() {
        return new AtomicInteger(0)::incrementAndGet;
    }

    public void compile() {
        elaborate();
        var manager = new ModelAnalysisManager(repo);
        manager.analyze(MetaElimination.class);
        manager.analyze(DataTypeInference.class);
        manager.analyze(AccessChecker.class);
        manager.analyze(CompositeDataBlaster.class);
        manager.analyze(IOAnalysis.class);
        manager.analyze(AliasAnalysis.class);
        manager.analyze(DuplicatedElimination.class);
        manager.analyze(RedundantElimination.class);
    }

    private JsonNode dump(PackageScope scope) {
        return new NxonDumper(scope).dump();
    }

    public JsonNode compileAndDump() {
        compile();
        return dump(repo.getTop());
    }

    public RepoScope elaborate() {
        PackageNode topPack;
        if (config.topPack() != null) {
            topPack = manager.resolvePackage(SourceLoc.none(), config.topPack());
        } else {
            topPack = manager.inferTopPack();
        }
        var scope = new Elaborator(repo, topPack, config).elaborate();
        repo.setTop(scope);
        repo.addChild(scope);
        return repo;
    }

    public ComponentScope elaborateComponent(PackageNode pack, Identifier top, ArgBindList parameters) {
        var newConfig = Configuration.builder().basedOn(config).withTop(top.name()).withArgs(parameters).build();
        var scope = new Elaborator(repo, pack, newConfig).elaborate();
        return scope.getTop();
    }

    public PackageManager getPackManager() {
        return manager;
    }

    public int nextDataIndex() {
        return dataIndexer.get();
    }

    /**
     * When the repo is imported in another repo, the compiler would serve as a sub-compiler of the parent repo,
     * that is, it would inherit the data indexer from the parent compiler.
     */
    public NXCompiler asSubCompilerOf(NXCompiler parent) {
        this.dataIndexer = parent.dataIndexer;
        return this;
    }
}
