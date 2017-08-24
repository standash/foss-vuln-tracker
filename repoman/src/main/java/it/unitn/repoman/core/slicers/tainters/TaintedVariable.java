package it.unitn.repoman.core.slicers.tainters;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class TaintedVariable {
	private final ParseTree variable;
	private final ParserRuleContext scope;
	
	public TaintedVariable(ParserRuleContext scope, ParseTree variable) {
		this.scope = scope;
		this.variable = variable;
	}
	
	public ParserRuleContext getScope() {
		return this.scope;
	}
	
	public ParseTree getVariable() {
		return this.variable;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[" + this.scope.getStart().getLine() + "] -> " + this.variable.getText() + "\n");
		return builder.toString();
	}
}
