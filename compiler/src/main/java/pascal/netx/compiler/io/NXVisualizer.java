package pascal.netx.compiler.io;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.compiler.model.variable.Data;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;

public class NXVisualizer {
    private final int depth;
    private final PackageScope design;
    private final NotepadWriter writer;

    private final Map<ComponentScope, Integer> componentMap = new HashMap<>();
    private final Map<Variable, String> nodeMap = new HashMap<>();

    public NXVisualizer(PackageScope design, int depth, NotepadWriter writer) {
        this.design = design;
        this.depth = depth;
        this.writer = writer;
    }

    private int allocateComponentId(ComponentScope scope) {
        return componentMap.computeIfAbsent(scope, _ -> componentMap.size());
    }

    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    private void printCellComponent(ComponentScope scope) {
        StringBuilder html = new StringBuilder();
        html.append("<table>").append("<tr>").append("<td><table>");
        for (int i = 0; i < scope.getInputs().size(); i++) {
            html.append("<tr><td port=\"in_").append(i).append("\">•</td></tr>");
        }
        html.append("</table></td>");
        html.append("<td>").append(scope.getIdentifier().name()).append("</td>");
        html.append("<td><table>");
        for (int i = 0; i < scope.getOutputs().size(); i++) {
            html.append("<tr><td port=\"out_").append(i).append("\">•</td></tr>");
        }
        html.append("</table></td>");
        html.append("</tr></table>");

        var nodeName = quote(String.valueOf(allocateComponentId(scope)));
        writer.print(nodeName);
        writer.print("[");
        writer.print("label=<" + html + ">");
        writer.print("margin=0 shape=none");
        writer.println("]");

        Set<Variable> defs = new HashSet<>();
        scope.getBinding().forEachData(defs::add);
        for (int i = 0; i < scope.getInputs().size(); i++) {
            var input = scope.getInputs().get(i);
            var portName = nodeName + ":in_" + i + ":w";
            if (defs.contains(input)) {
                nodeMap.put(input, portName);
            }
        }
        for (int i = 0; i < scope.getOutputs().size(); i++) {
            var output = scope.getOutputs().get(i);
            var portName = nodeName + ":out_" + i + ":e";
            if (defs.contains(output)) {
                nodeMap.put(output, portName);
            }
        }
    }

    private static String getPortName(Variable input) {
        return "port" + switch (input) {
            case Data data -> String.valueOf(data.getIndex());
            case Access access -> String.valueOf(access.getIndex()) + access.selector(); // TODO:
            case Value value -> value.toString();
            default -> throw new ShallNotReachHereError();
        };
    }

    private void printPorts(String nodeName, List<Variable> portList) {
        for (var port : portList) {
            var portName = quote(nodeName + getPortName(port));
            writer.print(portName + " [color=black shape=point]");
        }
    }

    private void printFullComponent(ComponentScope scope, int depth) {
        var nodeName = String.valueOf(allocateComponentId(scope));
        var name = "cluster." + nodeName;
        writer.print("subgraph " + quote(name) + " {");
        writer.incIndent();
        writer.println("label=" + scope.getIdentifier().name());
        writer.println("labeljust = c");
        writer.println("labelloc = t");


        writer.println("{ rank=same"); printPorts(nodeName, scope.getSignature().inputs()); writer.println("}");
        for (var child : scope.getChildren()) {
            printComponent((ComponentScope) child, depth - 1);
        }
        writer.println("{ rank=same"); printPorts(nodeName, scope.getSignature().outputs()); writer.println("}");
        scope.getBinding().forEachData(data -> {
            for (var input : scope.getSignature().inputs()) {
                if (data == input) {
                    nodeMap.put(input, quote(nodeName + getPortName(input)));
                }
            }
            for (var output : scope.getSignature().outputs()) {
                if (data == output) {
                    nodeMap.put(output, quote(nodeName + getPortName(output)));
                }
            }
        });


        for (var child : scope.getChildren()) {
            var childScope = (ComponentScope) child;
            var childName = String.valueOf(allocateComponentId(childScope));
            if (childScope.isBuiltin()) {
                for (int i = 0; i < childScope.getInputs().size(); i++) {
                    var input = childScope.getInputs().get(i);
                    var connectName = quote(childName) + ":in_" + i + ":w";
                    if (nodeMap.containsKey(input) && !nodeMap.get(input).equals(connectName)) {
                        writer.println(nodeMap.get(input) + "->" + connectName);
                    }
                }
                for (int i = 0; i < childScope.getOutputs().size(); i++) {
                    var output = childScope.getOutputs().get(i);
                    var connectName = quote(childName) + ":out_" + i + ":e";
                    if (nodeMap.containsKey(output) && !nodeMap.get(output).equals(connectName)) {
                        writer.println(connectName + "->" + nodeMap.get(output));
                    }
                }
            } else {
                var inputIter = childScope.getInputs().iterator();
                for (var input : childScope.getSignature().inputs()) {
                    var par = inputIter.next();
                    if (!getPortName(input).equals(getPortName(par)) && !(par instanceof Value)) {
                        var parentPort = quote(nodeName + getPortName(par));
                        var childPort = quote(childName + getPortName(input));
                        writer.println(parentPort + "->" + childPort);
                    }
                }
                var outputIter = childScope.getOutputs().iterator();
                for (var output : childScope.getSignature().outputs()) {
                    var par = outputIter.next();
                    if (!getPortName(output).equals(getPortName(par)) && !(par instanceof Value)) {
                        var parentPort = quote(childName + getPortName(output));
                        var childPort = quote(nodeName + getPortName(par));
                        writer.println(parentPort + "->" + childPort);
                    }
                }
            }
        }

        writer.decIndent();
        writer.println("}");
    }

    private void printComponent(ComponentScope scope, int depth) {
        if (depth > 0 && !scope.isBuiltin()) {
            printFullComponent(scope, depth - 1);
        } else {
            printCellComponent(scope);
        }
    }

    public void visualize() {
        writer.println("digraph {");
        writer.incIndent();
        writer.println("compound=true;");
        writer.println("rankdir=\"LR\"");

        printFullComponent(design.getTop(), depth - 1);

        writer.decIndent();
        writer.println("}");
        writer.flush();
    }
}
