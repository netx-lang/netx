package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public abstract class GraphRule implements Rule {
    protected final Graph graph;
    protected static final Map<Vertex, BitSet> record = new HashMap<>();

    protected GraphRule(Graph graph) {
        this.graph = graph;
    }

    protected boolean hasMarker(Vertex vertex, Marker marker) {
        return record.getOrDefault(vertex, new BitSet(Marker.size())).get(marker.asIndex());
    }

    protected void mark(Vertex vertex, Marker marker) {
        record.computeIfAbsent(vertex, _ -> new BitSet(Marker.size())).set(marker.asIndex());
    }

    protected void migrateMarkers(Vertex from, Vertex to) {
        var frm = record.computeIfAbsent(from, _ -> new BitSet(Marker.size()));
        var tom = record.computeIfAbsent(to, _ -> new BitSet(Marker.size()));
        tom.or(frm);
    }

    public enum Marker {
        DE_MORGAN_PUSH,
        DE_MORGAN_PULL,
        RETIMING_PUSH,
        RETIMING_PULL,
        DISTRIBUTIVITY_PULL,
        ;

        static int size() {
            return Marker.values().length;
        }

        public int asIndex() {
            return this.ordinal();
        }
    }
}

