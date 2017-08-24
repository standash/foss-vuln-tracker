package it.unitn.repoman.core.lang.traversals.generic;

import java.util.Set;
import java.util.HashSet;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class ConditionTraversal extends DFSTraversal {
	private Set<ParserRuleContext> innerStmts = new HashSet<>();

	public ConditionTraversal(ParserRuleContext cnd) {
		traverse(cnd.getParent());
	}

	@Override 
	public void onEnterNode(ParseTree node) {
		if (wrapper.isStatement(node)) {
			innerStmts.add((ParserRuleContext)node);
		}
	}

	public Set<ParserRuleContext> getInnerStatements() {
		return this.innerStmts;
	}
}