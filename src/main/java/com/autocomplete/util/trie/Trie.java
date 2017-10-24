package com.autocomplete.util.trie;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Trie<T> {

    private Node<T> root;

    public Trie() {
        this.root = new Node<>();
    }

    public Set<List<T>> getKeysWithPrefix(List<T> prefix) {
        Node<T> currentNode = root;

        for (T currentPrefixUnit : prefix) {
            if (currentNode.has(currentPrefixUnit)) {
                currentNode = currentNode.getChild(currentPrefixUnit);
            } else {
                return new HashSet<>();
            }
        }

        return currentNode.descendents;
    }

    public boolean add(List<T> sequence) {
        return root.add(sequence);
    }

    private class Node<T> {
        private Set<List<T>> descendents;
        private Map<T, Node<T>> children;

        Node() {
            this.descendents = new HashSet<>();
            this.children = new ConcurrentHashMap<>();
        }

        boolean has(T child) {
            return children.containsKey(child);
        }

        Node<T> getChild(T child) {
            return children.get(child);
        }

        boolean add(List<T> sequence) {
            return add(sequence, 0);
        }

        private boolean add(List<T> sequence, int depth) {
            if (depth >= sequence.size()) {
                return false;
            }

            descendents.add(sequence);

            T current = sequence.get(depth);

            if (!children.containsKey(current)) {
                children.put(current, new Node<T>());
            };

            Node<T> next = children.get(current);
            next.add(sequence, depth + 1);

            return true;
        }
    }
}
