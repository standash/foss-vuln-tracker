package it.unitn.repoman.core.lang.traversals.generic;

import java.util.LinkedHashSet;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class StatementMappingTraversal extends DFSTraversal {

    private final Set<ParserRuleContext> methodDeclarations = new LinkedHashSet<>();
	private final Set<ParserRuleContext> statements = new LinkedHashSet<>();
	
	@Override
    public void onEnterNode(ParseTree node) {
		if (wrapper.isCollectable(node)) {
			this.statements.add((ParserRuleContext) node);
		}
		else if (wrapper.isMethodDeclaration(node)) {
		    this.methodDeclarations.add((ParserRuleContext) node);
        }
	}
	
	public Set<ParserRuleContext> getStatements() {
		return this.statements;
	}

	public Set<ParserRuleContext> getMethodDeclarations() {
	    return this.methodDeclarations;
    }
}
