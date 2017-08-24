package it.unitn.repoman.core.lang.traversals.generic;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.LinkedList;
import java.util.List;

public class DotSymbolTraversal extends DFSTraversal{

    private ParseTree rightmostNode = null;
    private List<ParseTree> nodes = new LinkedList<>();

    public DotSymbolTraversal(ParseTree root) {
        traverse(root);
        rightmostNode = nodes.get(nodes.size()-1);
    }

    @Override
    public void onEnterNode(ParseTree node) {
        if (wrapper.isTerminal(node)) {
            if (!node.getText().equals(".")) {
                nodes.add(node);
            }
        }
    }

    public ParseTree getRightmostNode() {
        return rightmostNode;
    }

    public String getRightmostNodeName() {
        return (rightmostNode != null) ? rightmostNode.getText() : "";
    }

    public List<ParseTree> getLeftNodes() {
        List<ParseTree> leftNodes = new LinkedList<>();
        leftNodes.addAll(nodes);
        if (rightmostNode != null) {
            leftNodes.remove(rightmostNode);
        }
        return leftNodes;
    }

    public List<String> getLeftNodesNames() {
        List<String> leftNodesNames = new LinkedList<>();
        for (ParseTree node : getLeftNodes()) {
            leftNodesNames.add(node.getText());
        }
        return leftNodesNames;
    }
}
