package it.unitn.repoman.core.lang.traversals.generic;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.LinkedList;
import java.util.List;

public class MethodDeclarationTraversal extends DFSTraversal {

    private ParseTree methodName = null;
    private ParseTree returnType = null;
    private List<String> paramNames = new LinkedList<>();
    private List<ParseTree> paramNodes = new LinkedList<>();
    private final List<ParserRuleContext> parameters = new LinkedList<>();

    public MethodDeclarationTraversal(ParserRuleContext ctx) {
        if (wrapper.isMethodDeclaration(ctx)) {
            traverse(ctx);
        } // or else throw an exception?
        for (ParserRuleContext param : parameters) {
            FormalParameterTraversal t = new FormalParameterTraversal(param);
            paramNames.add(t.getVariableName());
            paramNodes.add(t.getVariableTerminalNode());
        }
    }

    @Override
    public void onEnterNode(ParseTree node) {
        if (returnType == null && wrapper.isTerminal(node)) {
            returnType = node;
        }
        else if (methodName == null && wrapper.isTerminal(node)) {
            methodName = node;
        }
        else if (methodName != null && returnType != null){
            if (wrapper.isFormalParameter(node)) {
                parameters.add((ParserRuleContext) node);
            }
        }
    }

    public String getMethodName() {
        return methodName.getText();
    }

    public String getReturnType() {
        return returnType.getText();
    }

    public List<String> getParamNames() {
       return paramNames;
    }

    public int getParamsNumber()  {
        return parameters.size();
    }

    public String getParamName(int pos) {
        return paramNames.get(pos);
    }

    public ParseTree getParamNode(int pos) {
        return paramNodes.get(pos);
    }

    public ParseTree getParam(int pos) {
        return parameters.get(pos);
    }
}
