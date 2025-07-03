package pascal.netx.compiler.ast;

import pascal.netx.compiler.ast.element.AbstractComponentNode;
import pascal.netx.compiler.ast.element.PackageElement;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class PackageNode extends ASTNode {
    private List<PackageElement> elements = new ArrayList<>();
    private PackageInfoNode packageInfo;

    private PackageNode(PackageInfoNode packageInfo) {
        this.packageInfo = packageInfo;
    }

    public static PackageNode of(PackageInfoNode packageInfo) {
        return new PackageNode(packageInfo);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        elements.forEach(either -> either.walk(listener));
        listener.exit(this);
    }

    public List<PackageElement> getElements() {
        return elements;
    }

    public void setElements(List<PackageElement> elements) {
        this.elements = elements;
    }

    public AbstractComponentNode resolveComponent(Identifier identifier) {
        for (var element : elements) {
            if (element instanceof AbstractComponentNode componentNode &&
                componentNode.getIdentifier().equals(identifier)) {
                return componentNode;
            }
        }
        throw new NoSuchElementException("Cannot resolve component: " + identifier);
    }

    public PackageInfoNode getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfoNode packageInfo) {
        this.packageInfo = packageInfo;
    }

    public boolean isApplication() {
        return packageInfo.getKind() == PackageInfoNode.Kind.APPLICATION;
    }

    public void addDefinition(PackageElement element) {
        elements.add(element);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PackageNode) obj;
        return Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(packageInfo);
        for (var definition : elements) {
            sb.append(definition).append('\n');
        }
        return sb.toString();
    }
}
