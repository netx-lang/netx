package pascal.hardscript.compiler.analysis;

import pascal.hardscript.compiler.model.instance.Array;
import pascal.hardscript.compiler.model.instance.DataBundle;
import pascal.hardscript.compiler.model.instance.DataComposition;
import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CompositeDataBlaster extends ModelAnalysis {
    public CompositeDataBlaster(GlobalScope design) {
        super(design);
    }

    private static List<Instance> blastFor(List<Instance> ports) {
        List<Instance> result = new ArrayList<>();
        for (var inst : ports) {
            if (Objects.requireNonNull(inst) instanceof DataComposition composition) {
                result.addAll(composition.flatten());
            } else {
                result.add(inst);
            }
        }
        return result;
    }

    private static boolean isComposite(Instance instance) {
        return instance instanceof DataBundle || instance instanceof Array;
    }

    @Override
    public void analyze() {
        design.forEachSubScope(this::analyze);
    }

    private void analyze(ComponentScope scope) {
        List<Instance> inputs = scope.getInputs(), outputs = scope.getOutputs();
        if (inputs.stream().anyMatch(CompositeDataBlaster::isComposite)) {
            scope.setInputs(blastFor(inputs));
        }
        if (outputs.stream().anyMatch(CompositeDataBlaster::isComposite)) {
            scope.setOutputs(blastFor(outputs));
        }

        if (scope.getSignature() != null) {
            var signature = scope.getSignature();
            if (signature.inputs().stream().anyMatch(CompositeDataBlaster::isComposite)) {
                signature.setInputs(blastFor(signature.inputs()));
            }
            if (signature.outputs().stream().anyMatch(CompositeDataBlaster::isComposite)) {
                signature.setOutputs(blastFor(signature.outputs()));
            }
        }
    }
}
