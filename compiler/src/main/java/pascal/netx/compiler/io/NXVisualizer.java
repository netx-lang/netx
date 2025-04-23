package pascal.netx.compiler.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Access;
import pascal.netx.compiler.model.variable.Data;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.compiler.model.variable.selector.PartSelector;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;

public class NXVisualizer {
    private static final Logger log = LoggerFactory.getLogger(NXVisualizer.class);
    private final int depth;
    private final PackageScope design;
    private final NotepadWriter writer;

    private final Map<ComponentScope, Integer> componentMap = new HashMap<>();
    private final Map<Variable, String> nodeMap = new HashMap<>();
    private final Set<String> accessNode = new HashSet<>();
    private int constantIndex = 0;

    public NXVisualizer(PackageScope design, int depth, NotepadWriter writer) {
        this.design = design;
        this.depth = depth;
        this.writer = writer;
    }

    public void visualize() {
        writer.println("digraph {");
        writer.incIndent();
        writer.println("graph [splines=spline nodesep=0.2]");
        writer.println("rankdir=\"LR\"");
        printFullComponent(design.getTop(), depth);
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

    private String getPortName(Variable input) {
        return switch (input) {
            case Data data -> "port" + allocateComponentId(data.getParent()) + "_" + data.getIndex();
            case Access access -> {
                var baseName = "port" + allocateComponentId(access.getParent()) + "_" + access.getIndex();
                var name = switch (access.selector()) {
                    case BitSelector(var index) -> baseName + "_" + index.toInt();
                    case PartSelector(var from, var to) -> baseName + "_" + from.toInt() + "_" + to.toInt();
                };
                if (!accessNode.contains(name)) {
                    if (nodeMap.containsKey(access.base())) {
                        baseName = nodeMap.get(access.base());
                    } else {
                        baseName = quote(baseName);
                    }
                    writer.print(name);
                    writer.print("[");
                    writer.print("label=\"" + access.base() + access.selector() + "\" ");
                    writer.print("shape=plain style=filled fillcolor=\"Bisque\"");
                    writer.print(" group = "+ baseName);
                    writer.println("]");
                    accessNode.add(name);
                    writer.println(baseName + " -> " + name + "[dir=none style=dotted]");
                    writer.println(name + " -> " + baseName + "[style=invis]");
                }
                yield name;
            }
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
                nodeMap.put(input, nodeName + ":in_" + i);
            }
        }
        for (int i = 0; i < scope.getOutputs().size(); i++) {
            var output = scope.getOutputs().get(i);
            if (defs.contains(output)) {
                nodeMap.put(output, nodeName + ":out_" + i);
            }
        }
    }

    private void maintainNodeMapForFull(ComponentScope scope) {
        var defs = getDefs(scope);
        for (var input : scope.getSignature().inputs()) {
            if (defs.contains(input)) {
                nodeMap.put(input, quote(getPortName(input)));
            }
        }
        for (var output : scope.getSignature().outputs()) {
            if (defs.contains(output)) {
                nodeMap.put(output, quote(getPortName(output)));
            }
        }
    }

    private void printCellComponent(ComponentScope scope) {
        StringBuilder html = new StringBuilder();
        html.append("<table bgcolor=\"SeaShell\">").append("<tr>").append("<td bgcolor=\"Bisque\"><table>");
        for (int i = 0; i < scope.getInputs().size(); i++) {
            html.append("<tr><td port=\"in_").append(i).append("\">•</td></tr>");
        }
        html.append("</table></td>");
        html.append("<td>").append(scope.getIdentifier().name()).append("</td>");
        html.append("<td bgcolor=\"Bisque\"><table>");
        for (int i = 0; i < scope.getOutputs().size(); i++) {
            html.append("<tr><td port=\"out_").append(i).append("\">•</td></tr>");
        }
        html.append("</table></td>");
        html.append("</tr></table>");

        var nodeName = quote(String.valueOf(allocateComponentId(scope)));
        writer.print(nodeName);
        writer.print("[");
        writer.print("label=<" + html + ">");
        writer.print("margin=\"0,0.2\" shape=none");
        writer.println("]");

        maintainNodeMapForCell(scope, nodeName);
    }

    private void printPortList(List<Variable> portList, String nodeName) {
        for (var port : portList.reversed()) {
            var portName = quote(getPortName(port));
            writer.print(portName);
            writer.print("[");
            writer.print("label=\"" + port + "\"");
            writer.print("style=filled fillcolor=\"SeaShell\" shape=box");
            writer.print(" group=\"" + nodeName + "\"");
            writer.print("]");
            writer.newLine();
        }
    }

    private void printIOPorts(ComponentScope scope, String nodeName) {
        writer.println("{ rank=same");
        printPortList(scope.getSignature().inputs(), nodeName);
        writer.println("}");
        writer.println("{ rank=same");
        printPortList(scope.getSignature().outputs(), nodeName);
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
        writer.println("pencolor = seagreen");
        writer.println("penwidth = 2");
        printIOPorts(scope, nodeName);
        for (var child : scope.getChildren()) {
            printComponent((ComponentScope) child, depth - 1);
        }
        maintainNodeMapForFull(scope);
        for (var child : scope.getChildren()) {
            var childScope = (ComponentScope) child;
            if (needDump(childScope)) {
                var childName = String.valueOf(allocateComponentId(childScope));
                if (childScope.isBuiltin() || depth <= 1) {
                    connectForCell(childScope, childName);
                } else {
                    connectForFull(childScope);
                }
            }
        }

        writer.decIndent();
        writer.println("}");
    }

    private static boolean needDump(ComponentScope childScope) {
        if (childScope.isBuiltin()) {
            var builtin = BuiltinComponent.of(childScope.getIdentifier());
            return !BuiltinComponent.NO_DUMP.contains(builtin);
        }
        return true;
    }

    private String newConstant(Value value) {
        var nodeName = quote("const" + constantIndex++);
        writer.print(nodeName);
        writer.print("[");
        writer.print("label=" + quote(value.toString()));
        writer.print("shape=cds style=filled fillcolor=\"SeaShell\"");
        writer.println("]");
        return nodeName;
    }

    private void addEdge(String src, String dst) {
        writer.println(src + ":e -> " + dst + ":w [arrowsize=0.5 color=\"Navy\" penwidth=1.5]");
    }

    private void connectForFull(ComponentScope childScope) {
        var inputIter = childScope.getInputs().iterator();
        for (var input : childScope.getSignature().inputs()) {
            var arg = inputIter.next();
            if (arg instanceof Value) {
                addEdge(newConstant((Value) arg), quote(getPortName(input)));
            } else if (!getPortName(input).equals(getPortName(arg))) {
                addEdge(quote(getPortName(arg)), quote(getPortName(input)));
            }
        }
        var outputIter = childScope.getOutputs().iterator();
        for (var output : childScope.getSignature().outputs()) {
            var arg = outputIter.next();
            if (arg instanceof Value) {
                log.warn("{} constant output: {}", childScope.getSourceLoc(), arg);
            } else if (!getPortName(output).equals(getPortName(arg))) {
                addEdge(quote(getPortName(output)), quote(getPortName(arg)));
            }
        }
    }

    private void connectForCell(ComponentScope childScope, String childName) {
        for (int i = 0; i < childScope.getInputs().size(); i++) {
            var input = childScope.getInputs().get(i);
            var connectName = quote(childName) + ":in_" + i;
            if (input instanceof Value) {
                addEdge(newConstant((Value) input), connectName);
            } else if (nodeMap.containsKey(input) && !nodeMap.get(input).equals(connectName)) {
                addEdge(nodeMap.get(input), connectName);
            } else if (input instanceof Access && !connectName.equals(getPortName(input))) {
                addEdge(quote(getPortName(input)), connectName);
            }
        }
        for (int i = 0; i < childScope.getOutputs().size(); i++) {
            var output = childScope.getOutputs().get(i);
            var connectName = quote(childName) + ":out_" + i;
            if (output instanceof Value) {
                log.warn("{} constant output: {}", childScope.getSourceLoc(), output);
            } else if (nodeMap.containsKey(output) && !nodeMap.get(output).equals(connectName)) {
                addEdge(connectName, nodeMap.get(output));
            } else if (output instanceof Access && !connectName.equals(getPortName(output))) {
                addEdge(connectName, quote(getPortName(output)));
            }
        }
    }

    private void printComponent(ComponentScope scope, int depth) {
        if (depth > 0 && !scope.isBuiltin()) {
            printFullComponent(scope, depth);
        } else {
            if (needDump(scope)) {
                printCellComponent(scope);
            }
        }
    }
}
