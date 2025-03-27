package pascal.netx.compiler.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(NXVisualizer.class);
    private final int depth;
    private final PackageScope design;
    private final NotepadWriter writer;

    private final Map<ComponentScope, Integer> componentMap = new HashMap<>();
    private final Map<Variable, String> nodeMap = new HashMap<>();
    private int constantIndex = 0;

    public NXVisualizer(PackageScope design, int depth, NotepadWriter writer) {
        this.design = design;
        this.depth = depth;
        this.writer = writer;
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

    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    private static Set<Variable> getDefs(ComponentScope scope) {
        Set<Variable> defs = new HashSet<>();
        scope.getBinding().forEachData(defs::add);
        return defs;
    }

    private static String getPortName(Variable input) {
        return "port" + switch (input) {
            case Data data -> String.valueOf(data.getIndex());
            case Access access -> String.valueOf(access.getIndex()) + access.selector(); // TODO:
            case Value value -> value.toString();
            default -> throw new ShallNotReachHereError();
        };
    }

    private int allocateComponentId(ComponentScope scope) {
        return componentMap.computeIfAbsent(scope, _ -> componentMap.size());
    }

    private void maintainNodeMapForCell(ComponentScope scope, String nodeName) {
        var defs = getDefs(scope);
        for (int i = 0; i < scope.getInputs().size(); i++) {
            var input = scope.getInputs().get(i);
            if (defs.contains(input)) {
                nodeMap.put(input, nodeName + ":in_" + i + ":w");
            }
        }
        for (int i = 0; i < scope.getOutputs().size(); i++) {
            var output = scope.getOutputs().get(i);
            if (defs.contains(output)) {
                nodeMap.put(output, nodeName + ":out_" + i + ":e");
            }
        }
    }

    private void maintainNodeMapForFull(ComponentScope scope, String nodeName) {
        var defs = getDefs(scope);
        for (var input : scope.getSignature().inputs()) {
            if (defs.contains(input)) {
                nodeMap.put(input, quote(nodeName + getPortName(input)));
            }
        }
        for (var output : scope.getSignature().outputs()) {
            if (defs.contains(output)) {
                nodeMap.put(output, quote(nodeName + getPortName(output)));
            }
        }
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

        maintainNodeMapForCell(scope, nodeName);
    }

    private void printPortList(String nodeName, List<Variable> portList) {
        for (var port : portList) {
            var portName = quote(nodeName + getPortName(port));
            writer.println(portName + " [color=black shape=record label=\"" + port + "\"]");
        }
    }

    private void printIOPorts(ComponentScope scope, int depth, String nodeName) {
        writer.println("{ rank=same");
        printPortList(nodeName, scope.getSignature().inputs());
        writer.println("}");
        for (var child : scope.getChildren()) {
            printComponent((ComponentScope) child, depth - 1);
        }
        writer.println("{ rank=same");
        printPortList(nodeName, scope.getSignature().outputs());
        writer.println("}");
    }

    private void printFullComponent(ComponentScope scope, int depth) {
        var nodeName = String.valueOf(allocateComponentId(scope));
        var name = "cluster." + nodeName;
        writer.print("subgraph " + quote(name) + " {");
        writer.incIndent();
        writer.println("label=" + scope.getIdentifier().name());
        writer.println("labeljust = c");
        writer.println("labelloc = t");
        printIOPorts(scope, depth, nodeName);

        maintainNodeMapForFull(scope, nodeName);
        for (var child : scope.getChildren()) {
            var childScope = (ComponentScope) child;
            var childName = String.valueOf(allocateComponentId(childScope));
            if (childScope.isBuiltin() || depth <= 0) {
                connectForCell(childScope, childName);
            } else {
                connectForFull(childScope, nodeName, childName);
            }
        }

        writer.decIndent();
        writer.println("}");
    }

    private String newConstant(Value value) {
        var nodeName = quote("const" + constantIndex++);
        writer.print(nodeName);
        writer.print("[");
        writer.print("label=" + quote(value.toString()));
        writer.print("shape=record");
        writer.println("]");
        return nodeName;
    }

    private void connectForFull(ComponentScope childScope, String nodeName, String childName) {
        var inputIter = childScope.getInputs().iterator();
        for (var input : childScope.getSignature().inputs()) {
            var arg = inputIter.next();
            if (arg instanceof Value) {
                var argNode = newConstant((Value) arg);
                var childNode = quote(childName + getPortName(input));
                writer.println(argNode + "->" + childNode);
            } else {
                if (!getPortName(input).equals(getPortName(arg))) {
                    var argNode = quote(nodeName + getPortName(arg));
                    var childNode = quote(childName + getPortName(input));
                    writer.println(argNode + "->" + childNode);
                }
            }
        }
        var outputIter = childScope.getOutputs().iterator();
        for (var output : childScope.getSignature().outputs()) {
            var arg = outputIter.next();
            if (arg instanceof Value) {
                log.warn("{} constant output: {}", childScope.getSourceLoc(), arg);
            } else {
                if (!getPortName(output).equals(getPortName(arg))) {
                    var argNode = quote(childName + getPortName(output));
                    var childNode = quote(nodeName + getPortName(arg));
                    writer.println(argNode + "->" + childNode);
                }
            }
        }
    }

    private void connectForCell(ComponentScope childScope, String childName) {
        for (int i = 0; i < childScope.getInputs().size(); i++) {
            var input = childScope.getInputs().get(i);
            if (input instanceof Value) {
                var argNode = newConstant((Value) input);
                var connectName = quote(childName) + ":in_" + i + ":w";
                writer.println(argNode + "->" + connectName);
            } else {
                var connectName = quote(childName) + ":in_" + i + ":w";
                if (nodeMap.containsKey(input) && !nodeMap.get(input).equals(connectName)) {
                    writer.println(nodeMap.get(input) + "->" + connectName);
                }
            }
        }
        for (int i = 0; i < childScope.getOutputs().size(); i++) {
            var output = childScope.getOutputs().get(i);
            if (output instanceof Value) {
                log.warn("{} constant output: {}", childScope.getSourceLoc(), output);
            } else {
                var connectName = quote(childName) + ":out_" + i + ":e";
                if (nodeMap.containsKey(output) && !nodeMap.get(output).equals(connectName)) {
                    writer.println(connectName + "->" + nodeMap.get(output));
                }
            }
        }
    }

    private void printComponent(ComponentScope scope, int depth) {
        if (depth > 0 && !scope.isBuiltin()) {
            printFullComponent(scope, depth - 1);
        } else {
            printCellComponent(scope);
        }
    }
}
