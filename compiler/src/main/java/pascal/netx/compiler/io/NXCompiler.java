package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.compiler.analysis.*;
import pascal.netx.compiler.ast.PackageInfoNode;
import pascal.netx.compiler.ast.PackageNode;
import pascal.netx.compiler.elaborator.PackageElaborator;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.scope.ProjectScope;

import java.util.List;

public final class NXCompiler {
    private final PackageManager manager;
    private final Configuration config;
    private final List<PackageNode> packs;

    public NXCompiler(List<PackageNode> packs, Configuration config, PackageManager manager) {
        this.config = config;
        this.packs = packs;
        this.manager = manager;
    }

    public static NXCompiler forSinglePackage(PackageNode node, Configuration config) {
        return new NXCompiler(List.of(node), config, PackageManager.ofEmpty());
    }

    private static void checkType(PackageScope design) {
        new MetaElimination(design).analyze();
        new DataTypeInference(design).analyze();
        new AccessChecker(design).analyze();
        new CompositeDataBlaster(design).analyze();
        new IOAnalysis(design).analyze();
    }

    public ProjectScope elaborate() {
        // FIXME: project infomation, project elaborator
        var result = new ProjectScope(null);
        for (var pack : packs) {
            var scope = new PackageElaborator(result, pack, config).elaborate();
            result.addChild(scope);
            if (pack.getPackageInfo().getKind() == PackageInfoNode.Kind.APPLICATION) {
                result.setTop(scope);
            }
        }
        return result;
    }

    public JsonNode compile() {
        ProjectScope design = elaborate();
        design.forEachPackage(NXCompiler::checkType);
        if (config.fullyExpand()) {
            design.packages()
                    .map(PackageAliasAnalysis::new)
                    .forEach(ModelAnalysis::analyze);
            design.packages()
                    .map(PackageDuplicatedElimination::new)
                    .forEach(ModelAnalysis::analyze);
            return new ExpandedDumper(design.getTop()).dump();
        } else {
            design.packages()
                    .map(ComponentAliasAnalysis::new)
                    .forEach(ModelAnalysis::analyze);
            design.packages()
                    .map(ComponentDuplicatedElimination::new)
                    .forEach(ModelAnalysis::analyze);
            return new CompactDumper(design.getTop()).dump();
        }
    }
}
