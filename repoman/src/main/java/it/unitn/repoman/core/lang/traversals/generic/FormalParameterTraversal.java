package it.unitn.repoman.core.lang.traversals.generic;

import it.unitn.repoman.core.lang.parsers.java.JavaParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class FormalParameterTraversal extends DFSTraversal {

    private ParseTree type;
    private ParseTree variable;

    public FormalParameterTraversal(ParserRuleContext ctx) {
        if (wrapper.isFormalParameter(ctx)) {
            traverse(ctx);
        }
    }

    @Override
    public void onEnterNode(ParseTree node) {
        if (node.getClass().equals(JavaParser.TypeTypeContext.class)) {
            type = node;
        }
        else if (node.getClass().equals(JavaParser.VariableDeclaratorIdContext.class)) {
            variable = node;
        }
    }

    public String getTypeName() {
        return type.getText();
    }

    public String getVariableName() {
        return variable.getText();
    }

    public ParseTree getTypeTerminalNode() {
        return type.getChild(0);
    }

    public ParseTree getTypeNode() {
        return type;
    }

    public ParseTree getVariableTerminalNode() {
        return variable.getChild(0);
    }

    public ParseTree getVariableNode() {
        return variable;
    }

}
