package it.unitn.repoman.core.lang.traversals.generic;

import org.antlr.v4.runtime.tree.ParseTree;

public interface Traversal {
    void traverse(ParseTree startNode);
    void onEnterNode(ParseTree node);
    void onExitNode(ParseTree node);
}
