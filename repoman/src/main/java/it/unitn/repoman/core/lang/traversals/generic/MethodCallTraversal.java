package it.unitn.repoman.core.lang.traversals.generic;

import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;


public abstract class MethodCallTraversal extends DFSTraversal {
	protected boolean isMethodCall = false;
	protected ParseTree callNameNode = null; 
	protected ParseTree paramsNode = null;
	protected final List<ParseTree> params = new LinkedList<>();
    protected final List<String> paramNames = new LinkedList<>();
    protected final int numberOfParams;
	protected int skip = -1;
	protected ParseTree methodContext = null;
	
	
	public MethodCallTraversal(ParseTree node) {
		traverse(node);
        for (ParseTree t : params) {
            if (wrapper.isToken(t)) {
                paramNames.add(t.getText());
            }
        }
        numberOfParams = params.size();
	}

	public String getMethodName() {
		return (callNameNode != null) ? callNameNode.getText() : "";
	}
	
	public int getParamsNumber() {
        return numberOfParams;
	}
	
	public ParseTree getCallNameNode() {
		return this.callNameNode;
	}
	
	public List<ParseTree> getParams() {
		return this.params;
	}

	public List<String> getParamNames() {
        return paramNames;
    }
	
	public ParseTree getParamsNode() {
		return this.paramsNode;
	}
	
	public boolean isMethodCall() {
		return this.isMethodCall;
	}
	
	public ParseTree getMethodContextNode() {
		return this.methodContext;
	}

    public String getParamName(int pos)	{
        return getParamNames().get(pos);
    }
}
