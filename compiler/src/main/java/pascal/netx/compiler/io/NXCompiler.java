package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.compiler.analysis.*;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.elaborator.BuiltInComponentFactory;
import pascal.netx.compiler.elaborator.PackageElaborator;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.ProjectScope;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.print.NotepadWriter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class NXCompiler {
    private final Configuration config;
    private final PackageManager manager;

    private final ProjectScope project;

    private final BuiltInComponentFactory factory;
    private Supplier<Integer> dataIndexer;

    public NXCompiler(ProjectScope.Info info, Configuration config, PackageManager manager) {
        this.config = config;
        this.manager = manager;
        this.project = new ProjectScope(info, manager);
        project.setCompiler(this);
        manager.setProject(project);
        this.dataIndexer = newIndexer();
        this.factory = new BuiltInComponentFactory(this);
    }

    public static NXCompiler forSinglePackage(PackageNode node, Configuration config) {
        var manager = PackageManager.ofEmpty();
        manager.addPackage(node);
        return new NXCompiler(ProjectScope.Info.empty(), config, manager);
    }

    private static void checkType(PackageScope design) {
        new MetaElimination(design).analyze();
        new DataTypeInference(design).analyze();
        new AccessChecker(design).analyze();
        new CompositeDataBlaster(design).analyze();
        new IOAnalysis(design).analyze();
    }

    private static void analyzeCompact(ProjectScope design) {
        design.packages()
                .map(ComponentAliasAnalysis::new)
                .forEach(ModelAnalysis::analyze);
        design.packages()
                .map(ComponentDuplicatedElimination::new)
                .forEach(ModelAnalysis::analyze);
    }

    private static void analyzeExpanded(ProjectScope design) {
        design.packages()
                .map(PackageAliasAnalysis::new)
                .forEach(ModelAnalysis::analyze);
        design.packages()
                .map(PackageDuplicatedElimination::new)
                .forEach(ModelAnalysis::analyze);
    }

    public ProjectScope elaborate() {
        var scope = new PackageElaborator(project, manager.findApplicationPack(), config).elaborate();
        project.setTop(scope);
        project.addChild(scope);
        return project;
    }

    public ComponentScope elaborate(PackageNode pack, Identifier top, List<Entity> parameters) {
        var newConfig = config.forAnotherTop(top.name()).withBindings(parameters);
        var scope = new PackageElaborator(project, pack, newConfig).elaborate();
        return scope.getTop();
    }

    public JsonNode compile() {
        ProjectScope design = elaborate();
        design.forEachPackage(NXCompiler::checkType);
        if (config.fullyExpand()) {
            analyzeExpanded(design);
            return new ExpandedDumper(design.getTop()).dump();
        } else {
            analyzeCompact(design);
            return new CompactDumper(design.getTop()).dump();
        }
    }

    public void visualize(NotepadWriter notepad, int depth) {
        if (config.fullyExpand()) {
            throw new ShallNotReachHereError();
        }
        ProjectScope design = elaborate();
        analyzeCompact(design);
        new NXVisualizer(design.getTop(), depth, notepad).visualize();
    }

    public PackageManager getPackManager() {
        return manager;
    }

    public static Supplier<Integer> newIndexer() {
        return new AtomicInteger(0)::incrementAndGet;
    }

    public Supplier<Integer> getDataIndexer() {
        return dataIndexer;
    }

    public void setDataIndexer(Supplier<Integer> indexer) {
        dataIndexer = indexer;
    }

    public BuiltInComponentFactory getBuiltinFactory() {
        return factory;
    }
}
