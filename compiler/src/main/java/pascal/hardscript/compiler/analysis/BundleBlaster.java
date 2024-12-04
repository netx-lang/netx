package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.instance.DataBundle;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

import java.util.ArrayList;
import java.util.List;

public class BundleBlaster extends ModelAnalysis {
    public BundleBlaster(GlobalScope design) {
        super(design);
    }

    private static List<Instance> blastFor(List<Instance> ports) {
        List<Instance> result = new ArrayList<>();
        for (var inst : ports) {
            if (inst instanceof DataBundle bundle) {
                result.addAll(bundle.flatten());
            } else {
                result.add(inst);
            }
        }
        return result;
    }

    @Override
    public void analyze() {
        design.forEachSubScope(this::analyze);
    }

    private void analyze(ComponentScope scope) {
        List<Instance> inputs = scope.getInputs(), outputs = scope.getOutputs();
        if (inputs.stream().anyMatch(DataBundle.class::isInstance)) {
            scope.setInputs(blastFor(inputs));
        }
        if (outputs.stream().anyMatch(DataBundle.class::isInstance)) {
            scope.setOutputs(blastFor(outputs));
        }

        if (scope.getSignature() != null) {
            var signature = scope.getSignature();
            if (signature.inputs().stream().anyMatch(DataBundle.class::isInstance)) {
                signature.setInputs(blastFor(signature.inputs()));
            }
            if (signature.outputs().stream().anyMatch(DataBundle.class::isInstance)) {
                signature.setOutputs(blastFor(signature.outputs()));
            }
        }
    }
}
