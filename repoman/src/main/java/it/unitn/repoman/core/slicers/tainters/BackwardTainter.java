package it.unitn.repoman.core.slicers.tainters;

import java.util.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import it.unitn.repoman.core.lang.traversals.generic.ConditionTraversal;

public class BackwardTainter extends ForwardTainter {

	public BackwardTainter(Set<ParserRuleContext> collectables, Set<ParserRuleContext> seeds) {
		super(collectables, seeds);
	}
	
	@Override
	protected void initSeed(ParserRuleContext stmt) {
		this.taintedStatements.add(stmt);
		ParserRuleContext scope = wrapper.getContainer(stmt);
		Set<ParseTree> refVars = wrapper.getReferencedVars(stmt);
		for (ParseTree refVar : refVars) {
			this.taints.addTaintedVariable(scope, refVar);
		}
		ParseTree defVar = wrapper.getDefinedVar(stmt);
		if (defVar != null) {
			this.taints.addTaintedVariable(scope, defVar);
		}
	}

	@Override
	public Set<ParserRuleContext> iterate(ParserRuleContext taintedStmt) {
		Set<ParserRuleContext> elementsToAdd = new LinkedHashSet<>();
		ParserRuleContext taintedScope = wrapper.getContainer(taintedStmt);
		
		LinkedList<ParserRuleContext> list = new LinkedList<>(this.collectables);
		Iterator<ParserRuleContext> backwardIter = list.descendingIterator();
		while (backwardIter.hasNext()) {
			ParserRuleContext collectable = backwardIter.next();
            if (taintedStatements.contains(collectable)) {
                continue;
            }
			ParserRuleContext collectableScope = wrapper.getContainer(collectable);
			if (collectableScope.equals(taintedScope) && wrapper.precedes(collectable, taintedStmt)) {
				Set<String> taintedVars = this.taints.getVariableNames(collectableScope);
				// if the seed is an inner statement of a conditional statement, take the conditional statement-----
				if (wrapper.isConditionalExpression(collectable)) {
					ConditionTraversal t = new ConditionTraversal(collectable);
					for (ParserRuleContext innerStmt : t.getInnerStatements()) {
						if (this.taintedStatements.contains(innerStmt) || elementsToAdd.contains(innerStmt)) {
							elementsToAdd.add(collectable);
							Set<ParseTree> referencedVars = wrapper.getReferencedVars(collectable);
							for (ParseTree referencedVar : referencedVars) {
								taints.addTaintedVariable(collectableScope, referencedVar);
							}
							break;
						}
					}
				}
				else {
					ParseTree definedVar = wrapper.getDefinedVar(collectable);
					if (definedVar != null) {
						if (taintedVars.contains(definedVar.getText())) {
							elementsToAdd.add(collectable);
							Set<ParseTree> referencedVars = wrapper.getReferencedVars(collectable);
							for (ParseTree referencedVar : referencedVars) {
								taints.addTaintedVariable(collectableScope, referencedVar);
							}
						}
					}
				}
			}
		}
		return elementsToAdd;
	}
	
}
