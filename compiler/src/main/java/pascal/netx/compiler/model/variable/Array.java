package pascal.netx.compiler.model.variable;

import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.type.ArrayType;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Array extends Composition {
    private final List<Mutable> elements = new ArrayList<>();

    private Array(Identifier id, ArrayType type, int index) {
        super(type, id, index);
    }

    @Override
    public List<Mutable> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public ArrayType getType() {
        return (ArrayType) super.getType();
    }

    @Override
    public ComponentScope getParent() {
        return elements.getFirst().getParent();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Array array = (Array) o;
        return Objects.equals(elements, array.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elements);
    }

    public static class Factory extends Mutable.Factory<Array> {
        private final Mutable.Factory<?> elementFactory;
        private final ArrayType type;
        private final int size;

        public Factory(NXCompiler compiler, int size, Mutable.Factory<?> elementFactory) {
            super(compiler);
            this.size = size;
            this.elementFactory = elementFactory;
            this.type = ArrayType.of(elementFactory.getType(), size);
        }

        @Override
        public Array create(ComponentScope parent, Identifier id) {
            var result = new Array(id, type, compiler.nextDataIndex());
            for (int i = 0; i < size; i++) {
                var elementID = Identifier.of(String.format("%s[%d]", id.name(), i))
                        .setSourceLoc(id.getSourceLoc());
                var element = elementFactory.create(parent, elementID);
                parent.bind(elementID, element);
                result.elements.add(element);
            }
            return result;
        }

        @Override
        public ArrayType getType() {
            return type;
        }
    }
}
