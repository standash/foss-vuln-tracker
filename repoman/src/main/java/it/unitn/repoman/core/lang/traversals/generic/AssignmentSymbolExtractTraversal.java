package it.unitn.repoman.core.lang.traversals.generic;

import java.util.LinkedList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class AssignmentSymbolExtractTraversal extends DFSTraversal {
	private ParserRuleContext stmt = null;
	private boolean isNextLeft = false;
	private ParseTree lhs = null;
	private List<ParseTree> rhs = new LinkedList<>();

	public AssignmentSymbolExtractTraversal(ParserRuleContext ctx) {
		this.stmt = ctx;
		traverse();
	}

    @Override
    public void traverse() {
    	this.traverse(this.stmt);
    }

	@Override
    public void onEnterNode(ParseTree node) {
		if (isWithinContext(node)) {
			isNextLeft = true;
		}
		if (isNextLeft) {
			if (wrapper.isTerminal(node) && wrapper.isToken(node)) {
				isNextLeft = false;
				lhs = node;
			}
		}
		else {
			SymbolExtractTraversal t = new SymbolExtractTraversal(stmt);
			rhs.addAll(t.getSymbols());
			rhs.remove(lhs);
			terminate();
		}
    }
	
	@Override
    protected boolean isWithinContext(ParseTree node) {
        return wrapper.isStatement(node.getParent()) || 
        		wrapper.isFieldDeclaration(node.getParent());
    }

    public ParseTree getLhs() {
    	return lhs;
    }

    public List<ParseTree> getRhsList() {
    	return rhs;
    }
}