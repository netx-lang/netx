package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.compiler.analysis.model.*;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.elaborator.PackageElaborator;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.RepoScope;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;
import pascal.netx.util.print.NotepadWriter;

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

    private static void checkType(PackageScope design) {
        new MetaElimination(design).analyze();
        new DataTypeInference(design).analyze();
        new AccessChecker(design).analyze();
        new CompositeDataBlaster(design).analyze();
        new IOAnalysis(design).analyze();
    }

    private static void analyzeCompact(RepoScope design) {
        design.packages()
                .map(ComponentAliasAnalysis::new)
                .forEach(ModelAnalysis::analyze);
        design.packages()
                .map(ComponentDuplicatedElimination::new)
                .forEach(ModelAnalysis::analyze);
    }

    private static void analyzeExpanded(RepoScope design) {
        design.packages()
                .map(PackageAliasAnalysis::new)
                .forEach(ModelAnalysis::analyze);
        design.packages()
                .map(PackageDuplicatedElimination::new)
                .forEach(ModelAnalysis::analyze);
    }

    public static Supplier<Integer> newIndexer() {
        return new AtomicInteger(0)::incrementAndGet;
    }

    public RepoScope elaborate() {
        PackageNode topPack;
        if (config.topPack() != null) {
            topPack = manager.resolvePackage(SourceLoc.none(), config.topPack());
        } else {
            topPack = manager.inferTopPack();
        }
        var scope = new PackageElaborator(repo, topPack, config).elaborate();
        repo.setTop(scope);
        repo.addChild(scope);
        return repo;
    }

    public ComponentScope elaborateComponent(PackageNode pack, Identifier top, ArgBindList parameters) {
        var newConfig = Configuration.builder().basedOn(config).withTop(top.name()).withArgs(parameters).build();
        var scope = new PackageElaborator(repo, pack, newConfig).elaborate();
        return scope.getTop();
    }

    public void compile() {
        elaborate();
        repo.forEachPackage(NXCompiler::checkType);
        if (config.fullyExpand()) {
            analyzeExpanded(repo);
        } else {
            analyzeCompact(repo);
        }
    }

    private JsonNode dump(PackageScope scope) {
        if (config.fullyExpand()) {
            return new ExpandedDumper(scope).dump();
        } else {
            return new CompactDumper(scope).dump();
        }
    }

    public JsonNode compileAndDump() {
        compile();
        return dump(repo.getTop());
    }

    public void compileAndVisualize(NotepadWriter notepad, int depth) {
        if (config.fullyExpand()) {
            throw new ShallNotReachHereError();
        }
        compile();
        new NXVisualizer(repo.getTop(), depth, notepad).visualize();
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
