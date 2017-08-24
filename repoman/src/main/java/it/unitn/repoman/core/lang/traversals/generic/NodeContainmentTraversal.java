package it.unitn.repoman.core.lang.traversals.generic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class NodeContainmentTraversal extends DFSTraversal {

    private boolean isFound = false;
    private final ParseTree soughtNode;

    public NodeContainmentTraversal(ParserRuleContext root, ParseTree node) {
        soughtNode = node;
        traverse(root);
    }

    @Override
    public void onEnterNode(ParseTree node) {
        if (node.equals(soughtNode)) {
            isFound = true;
            terminate();
        }
    }

    public boolean isFound() {
        return isFound;
    }
}
