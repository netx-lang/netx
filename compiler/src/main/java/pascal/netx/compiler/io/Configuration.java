package pascal.netx.compiler.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.compiler.model.scope.binding.MultiBinding;
import pascal.netx.compiler.model.variable.BitVectorValue;
import pascal.netx.compiler.model.variable.BooleanValue;
import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public record Configuration(boolean fullyExpand,
                            @Nullable Identifier topPack,
                            @Nonnull  Identifier topName,
                            @Nullable ArgBindList arguments) {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private Configuration(Builder builder) {
        this(builder.fullyExpand, builder.topPack, builder.topName, builder.arguments);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Configuration forExpandedTest() {
        return new Builder().forExpanded().build();
    }

    public static Configuration forCompactTest() {
        return new Builder().forCompact().build();
    }

    public static class Builder {
        private boolean fullyExpand;
        private Identifier topPack;
        private Identifier topName = Identifier.of("top");
        private ArgBindList arguments;

        public Builder basedOn(Configuration config) {
            this.fullyExpand = config.fullyExpand;
            this.topPack = config.topPack;
            this.topName = config.topName;
            this.arguments = config.arguments;
            return this;
        }

        public Builder forCompact() {
            this.fullyExpand = false;
            return this;
        }

        public Builder forExpanded() {
            this.fullyExpand = true;
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
                    return Int32Value.of(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    try {
                        return BitVectorValue.of(BitVector.parse(value));
                    } catch (Exception ignored) {
                        log.error("Invalid argument value: {}", value);
                        throw new IllegalArgumentException("Invalid argument value: " + value);
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
                    log.error("Invalid argument format: {}", arg);
                    throw new IllegalArgumentException("Invalid argument format: " + arg);
                }
            }
            return withArgs(bindings);
        }

        public Builder withArgs(ArgBindList arguments) {
            this.arguments = arguments;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}