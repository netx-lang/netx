package pascal.netx.compiler.backend.analysis.model;

import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.Array;
import pascal.netx.compiler.model.circuit.signal.Bundle;
import pascal.netx.compiler.model.circuit.signal.Composition;
import pascal.netx.compiler.model.circuit.signal.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CompositeDataBlaster is an analysis that flattens composite data types
 * (like Bundles and Arrays) in the inputs and outputs of components.
 */
public final class CompositionBlaster extends ModelAnalysis {
    public CompositionBlaster(PackageScope design) {
        super(design);
    }

    private static List<Signal> blastFor(List<Signal> ports) {
        List<Signal> result = new ArrayList<>();
        for (var inst : ports) {
            if (Objects.requireNonNull(inst) instanceof Composition composition) {
                result.addAll(composition.flatten());
            } else {
                result.add(inst);
            }
        }
        return result;
    }

    private static boolean isComposite(Signal signal) {
        return signal instanceof Bundle || signal instanceof Array;
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(this::analyze);
    }

    private void analyze(ComponentScope scope) {
        List<Signal> inputs = scope.getInputs(), outputs = scope.getOutputs();
        if (inputs.stream().anyMatch(CompositionBlaster::isComposite)) {
            scope.setInputs(blastFor(inputs));
        }
        if (outputs.stream().anyMatch(CompositionBlaster::isComposite)) {
            scope.setOutputs(blastFor(outputs));
        }

        if (scope.getSignature() != null) {
            var signature = scope.getSignature();
            if (signature.inputs().stream().anyMatch(CompositionBlaster::isComposite)) {
                signature.setInputs(blastFor(signature.inputs()));
            }
            if (signature.outputs().stream().anyMatch(CompositionBlaster::isComposite)) {
                signature.setOutputs(blastFor(signature.outputs()));
            }
        }
    }
}
