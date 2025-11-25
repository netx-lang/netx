package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.variable.Array;
import pascal.netx.compiler.circuit.variable.Bundle;
import pascal.netx.compiler.circuit.variable.Composition;
import pascal.netx.compiler.circuit.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CompositeDataBlaster is an analysis that flattens composite data types
 * (like Bundles and Arrays) in the inputs and outputs of components.
 */
public final class CompositeDataBlaster extends ModelAnalysis {
    public CompositeDataBlaster(PackageScope design) {
        super(design);
    }

    private static List<Variable> blastFor(List<Variable> ports) {
        List<Variable> result = new ArrayList<>();
        for (var inst : ports) {
            if (Objects.requireNonNull(inst) instanceof Composition composition) {
                result.addAll(composition.flatten());
            } else {
                result.add(inst);
            }
        }
        return result;
    }

    private static boolean isComposite(Variable variable) {
        return variable instanceof Bundle || variable instanceof Array;
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(this::analyze);
    }

    private void analyze(ComponentScope scope) {
        List<Variable> inputs = scope.getInputs(), outputs = scope.getOutputs();
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
