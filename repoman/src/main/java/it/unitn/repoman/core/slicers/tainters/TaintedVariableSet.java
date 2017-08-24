package it.unitn.repoman.core.slicers.tainters;

import java.util.*;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.lang.traversals.generic.NodeContainmentTraversal;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class TaintedVariableSet {

	private final Set<TaintedVariable> taintedVars = new LinkedHashSet<>();
	
	public void addTaintedVariable(TaintedVariable newVariable) {
//		for (TaintedVariable var : this.taintedVars) {
//			if (var.getScope().equals(newVariable.getScope())
//					&& var.getVariable().getText().equals(newVariable.getVariable().getText())) {
//				return;
//			}
//		}
        for (TaintedVariable var : this.taintedVars) {
            if (var.getVariable().equals(newVariable.getVariable())) {
                return;
            }
        }
		taintedVars.add(newVariable);
	}

	public void addTaintedVariable(ParserRuleContext scope, ParseTree variable) {
		this.addTaintedVariable((new TaintedVariable(scope, variable)));
	}
	
	public Set<String> getVariableNames(ParserRuleContext scope) {
		Set<String> vars = new LinkedHashSet<>();
		for (TaintedVariable var : this.taintedVars) {
			if (var.getScope().equals(scope)) {
				vars.add(var.getVariable().getText());
			}
		}
		return vars;
	}
	
	public Set<ParseTree> getVariables(ParserRuleContext scope) {
		Set<ParseTree> vars = new LinkedHashSet<>();
		for (TaintedVariable var : this.taintedVars) {
			if (var.getScope().equals(scope)) {
				vars.add(var.getVariable());
			}
		}
		return vars;
	}

	public List<ParseTree> getStatementVariables(ParserRuleContext statement) {
        List<ParseTree> vars = new LinkedList<>();
        ParserRuleContext scope = LanguageFactory.getWrapper().getContainer(statement);
        for (TaintedVariable var : this.taintedVars) {
            if (var.getScope().equals(scope)) {
                NodeContainmentTraversal t = new NodeContainmentTraversal(statement, var.getVariable());
                if (t.isFound()) {
                    vars.add(var.getVariable());
                }
            }
        }
        return vars;
    }
	
//	public void removeVariable(ParserRuleContext scope, ParseTree variable) {
//		Iterator<TaintedVariable> it = this.taintedVars.iterator();
//		while (it.hasNext()) {
//			TaintedVariable var = it.next();
//			if (var.getScope().equals(scope) &&
//					var.getVariable().getText().equals(variable.getText()))  {
//				it.remove();
//			}
//		}
//	}
	
	public boolean isVariableTainted(ParserRuleContext scope, ParseTree variable) {
		for (TaintedVariable var : this.taintedVars) {
			if (var.getScope().equals(scope) && var.getVariable().getText().equals(variable.getText())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (TaintedVariable var : this.taintedVars) {
			builder.append(var.toString());
		}
		return builder.toString();
	}
}
