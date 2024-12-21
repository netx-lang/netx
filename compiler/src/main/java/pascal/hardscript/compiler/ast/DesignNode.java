package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.element.PackageElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DesignNode extends ASTNode {
    private List<PackageElement> elements = new ArrayList<>();
    private PackageInfoNode packageInfo;

    private DesignNode(PackageInfoNode packageInfo) {
        this.packageInfo = packageInfo;
    }

    public static DesignNode of(PackageInfoNode packageInfo) {
        return new DesignNode(packageInfo);
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

    public PackageInfoNode getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfoNode packageInfo) {
        this.packageInfo = packageInfo;
    }

    public void addDefinition(PackageElement element) {
        elements.add(element);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DesignNode) obj;
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

    public enum Type {
        LIBRARY, APPLICATION
    }
}
