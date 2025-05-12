package pascal.netx.util.collection;

import java.util.*;

public class UnionFind<E> {

    private final Map<E, E> parent = new HashMap<>(); // the disjoint set

    private int componentSize = 0;

    public void add(E u) {
        parent.put(u, u);
        componentSize++;
    }

    public boolean contains(E u) {
        return parent.containsKey(u);
    }

    /**
     * Unions two equivalence classes represented by element {@code u} and {@code v}.
     * Elements will be added if not contained in the universe.
     */
    public void union(E u, E v) {
        if (!contains(u)) {
            add(u);
        }
        if (!contains(v)) {
            add(v);
        }
        E rootU = find(u), rootV = find(v);
        if (rootU == rootV) {
            return;
        }
        parent.put(rootU, rootV);
        componentSize--;
    }

    /**
     * Element-wise union.
     */
    public void zip(List<E> us, List<E> vs) {
        if (us.size() != vs.size()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < us.size(); ++i) {
            union(us.get(i), vs.get(i));
        }
    }

    /**
     * @return The representative element that is equivalent to {@code u}.
     * @throws IllegalArgumentException If neither {@link #add(E)} nor
     *                                  {@link #union(E, E)} is called on {@code u} before.
     */
    public E find(E u) {
        if (!contains(u)) {
            throw new IllegalArgumentException(u + " is not contained");
        }
        return recursiveFind(u);
    }

    private E recursiveFind(E u) {
        if (parent.get(u) == u) {
            return u;
        } else {
            E result = find(parent.get(u));
            parent.put(u, result);
            return result;
        }
    }


    public boolean connected(E u, E v) {
        return find(u) == find(v);
    }

    public boolean isEmpty() {
        return parent.isEmpty();
    }

    public int elementSize() {
        return parent.size();
    }

    public Collection<E> elements() {
        return parent.keySet();
    }

    public int componentSize() {
        return componentSize;
    }

    public Collection<E> representatives() {
        Set<E> result = new HashSet<>();
        for (var u : elements()) {
            result.add(find(u));
        }
        return result;
    }
}