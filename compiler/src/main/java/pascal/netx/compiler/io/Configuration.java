package pascal.netx.compiler.io;

import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.circuit.scope.binding.MultiBinding;
import pascal.netx.compiler.circuit.signal.BooleanValue;
import pascal.netx.compiler.circuit.signal.Value;
import pascal.netx.compiler.circuit.signal.VectorValue;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public record Configuration(@Nullable Identifier topPack,
                            @Nonnull  Identifier topName,
                            @Nullable AttrBindList attributes) {

    private Configuration(Builder builder) {
        this(builder.topPack, builder.topName, builder.arguments);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Configuration asDefault() {
        return builder().build();
    }

    public static class Builder {
        private Identifier topPack;
        private Identifier topName = Identifier.of("top");
        private AttrBindList arguments;

        public Builder basedOn(Configuration config) {
            this.topPack = config.topPack;
            this.topName = config.topName;
            this.arguments = config.attributes;
            return this;
        }

        public Builder withTop(String literal) {
            var parts = literal.split("\\.");
            if (parts.length == 1) {
                this.topName = Identifier.of(parts[0]);
            } else {
                this.topPack = Identifier.of(parts[0]);
                this.topName = Identifier.of(parts[1]);
            }
            return this;
        }

        private static Value parseValue(String value) {
            if ("true".equals(value) || "false".equals(value)) {
                return BooleanValue.of(Boolean.parseBoolean(value));
            } else {
                try {
                    return VectorValue.fromInt(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    try {
                        return VectorValue.of(BitVector.parse(value));
                    } catch (Exception ignored) {
                        Logger.error("Invalid argument rhs: " + value);
                        throw new IllegalArgumentException();
                    }
                }
            }
        }

        public Builder withArgs(List<String> literal) {
            var bindings = MultiBinding.ofEmpty();
            for (String arg : literal) {
                String[] parts = arg.split("=");
                if (parts.length == 2) {
                    bindings.bind(Identifier.of(parts[0]), parseValue(parts[1]));
                } else {
                    Logger.error("Invalid argument format: " + arg);
                    throw new IllegalArgumentException();
                }
            }
            return withArgs(bindings);
        }

        public Builder withArgs(AttrBindList arguments) {
            this.arguments = arguments;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}