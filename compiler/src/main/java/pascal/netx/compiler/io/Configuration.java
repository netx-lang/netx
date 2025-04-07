package pascal.netx.compiler.io;

import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record Configuration(boolean fullyExpand,
                            @Nullable Identifier topPack,
                            @Nonnull  Identifier topName,
                            @Nullable ArgBindList arguments) {

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

        public Builder withArgs(ArgBindList arguments) {
            this.arguments = arguments;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}