package pascal.netx.compiler.io;

import pascal.netx.compiler.model.circuit.EList;
import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.compiler.model.circuit.scope.binding.AttrBindList;
import pascal.netx.compiler.model.circuit.scope.binding.IdEntityMap;
import pascal.netx.compiler.model.circuit.signal.BooleanValue;
import pascal.netx.compiler.model.circuit.signal.VectorValue;
import pascal.netx.compiler.utils.ParseCancellationException;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

        private static final Pattern[] VALUE_PATTERNS = {
                Pattern.compile("true|false"),                           // Boolean
                Pattern.compile("-?\\d+"),                               // Integer
                Pattern.compile("([0-9]+)?'[hHoOdDbB]([0-9a-fA-F?_]+)"), // BitVector
                Pattern.compile("\\[(.)+]"),                             // EList
        };

        private static Entity parseValue(String value) {
            value = value.strip();
            for (int i = 0; i != VALUE_PATTERNS.length; ++i) {
                var matcher = VALUE_PATTERNS[i].matcher(value);
                if (matcher.matches()) {
                    return VALUE_PARSERS[i].parse(value);
                }
            }
            Logger.error("Invalid attr '{}' in configuration", value);
            throw new ParseCancellationException();
        }

        interface ValueParser {
            Entity parse(String value);
        }

        private static final ValueParser[] VALUE_PARSERS = {
                value -> BooleanValue.of(Boolean.parseBoolean(value)),
                value -> VectorValue.fromInt(Integer.parseInt(value)),
                value -> VectorValue.of(BitVector.parse(value)),
                value -> EList.of(Arrays.stream(value.substring(1, value.length() - 1).split(","))
                        .map(Builder::parseValue).toList()),
        };

        public Builder withArgs(List<String> literal) {
            var bindings = IdEntityMap.ofEmpty();
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
