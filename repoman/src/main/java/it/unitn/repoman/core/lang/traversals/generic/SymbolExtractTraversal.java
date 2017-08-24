package it.unitn.repoman.core.lang.traversals.generic;

import java.util.LinkedHashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class SymbolExtractTraversal extends DFSTraversal {

    private Set<ParseTree> symbols = new LinkedHashSet<>();

	public SymbolExtractTraversal(ParserRuleContext ctx) {
		MethodCallTraversal t = wrapper.getMethodCallTraversal(ctx);
		if (!t.isMethodCall()) {
			traverse(ctx);
		}
		else {
			this.symbols.addAll(t.getParams());
            DotSymbolTraversal dott = new DotSymbolTraversal(t.getCallNameNode());
            symbols.addAll(dott.getLeftNodes());
		}
	}


	@Override
    public void onEnterNode(ParseTree node) {
		if (isWithinContext(node) && wrapper.isToken(node) && wrapper.isToken(node)) {
			symbols.add(node);
    	}
    }

	@Override
	protected boolean isWithinContext(ParseTree node) {
		return wrapper.isTerminal(node);
	}
	
	public Set<ParseTree> getSymbols() {
		return this.symbols;
	}

}
