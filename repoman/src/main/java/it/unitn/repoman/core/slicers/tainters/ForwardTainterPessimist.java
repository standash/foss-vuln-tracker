package it.unitn.repoman.core.slicers.tainters;

import it.unitn.repoman.core.lang.traversals.generic.ConditionTraversal;
import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ForwardTainterPessimist extends ForwardTainter {

    public ForwardTainterPessimist(Set<ParserRuleContext> collectables, Set<ParserRuleContext> seeds) {
        super(collectables, seeds);
    }

    @Override
    protected void initSeed(ParserRuleContext stmt) {
		this.taintedStatements.add(stmt);
		ParserRuleContext scope = wrapper.getContainer(stmt);
		ParseTree defVar = wrapper.getDefinedVar(stmt);
		if (defVar != null) {
			this.taints.addTaintedVariable(scope, defVar);
		}
        /*
         *  THIS SHOULD NOT HAPPEN IN AN "EFFECT-FREE" SLICE!
         * taint ref. variables in a method call as well */
        if (wrapper.isMethodCall(stmt)) {
            for (ParseTree var : wrapper.getReferencedVars(stmt)) {
                this.taints.addTaintedVariable(scope, var);
            }
        }
		// if the seed is a conditional statement or a return statement, take all inner statements
		else if (wrapper.isConditionalExpression(stmt)) {
			this.taintedStatements.addAll(taintConditionalExpression(stmt));
            // and taint all variables within the condition
            Set<ParseTree> vars = wrapper.getReferencedVars(stmt);
            for (ParseTree var : vars) {
                taints.addTaintedVariable(scope, var);
            }
		}
	}

    @Override
	public Set<ParserRuleContext> iterate(ParserRuleContext taintedStmt) {
		Set<ParserRuleContext> elementsToAdd = new LinkedHashSet<>();
		ParserRuleContext taintedScope = wrapper.getContainer(taintedStmt);

		for (ParserRuleContext collectable: this.collectables) {
            ParserRuleContext collectableScope = wrapper.getContainer(collectable);
            // do nothing if a statement is already collected
            if (taintedStatements.contains(collectable)) {
                if (wrapper.isMethodCall(collectable)) {
                    for (ParseTree var : wrapper.getReferencedVars(collectable)) {
                        this.taints.addTaintedVariable(collectableScope, var);
                    }
                }
                continue;
            }
			if (collectableScope.equals(taintedScope) && wrapper.precedes(taintedStmt, collectable)) {
                Set<String> taintedVars = this.taints.getVariableNames(collectableScope);
                Set<ParseTree> referencedVars = wrapper.getReferencedVars(collectable);
                MethodCallTraversal t = wrapper.getMethodCallTraversal(collectable);
                if (t.isMethodCall()) {
                    List<ParseTree> vars = t.getParams();
                    boolean tainted = false;
                    for (ParseTree var : vars) {
                        if (taintedVars.contains(var.getText()))  {
                            tainted = true;
                            break;
                        }
                    }
                    if (tainted) {
                        for (ParseTree var : vars) {
                            taints.addTaintedVariable(collectableScope, var);
                        }
                    }
                }
                for (ParseTree referencedVar : referencedVars) {
                    if (taintedVars.contains(referencedVar.getText())) {
                        elementsToAdd.add(collectable);
                        ParseTree definedVar = wrapper.getDefinedVar(collectable);
                        if (definedVar != null) {
                            taints.addTaintedVariable(collectableScope, definedVar);
                        }
                    }
                }
			}
		}
		Set<ParserRuleContext> taintedInnerStatements = new HashSet<>();
		for (ParserRuleContext ctx : elementsToAdd) {
           if (wrapper.isConditionalExpression(ctx))  {
               taintedInnerStatements.addAll(taintInnerStatements(ctx));
           }
        }
        elementsToAdd.addAll(taintedInnerStatements);
		return elementsToAdd;
	}

	@Override
    protected Set<ParserRuleContext> taintInnerStatements(ParserRuleContext cnd) {
        Set<ParserRuleContext> addedStmts = new HashSet<>();
        ConditionTraversal t = new ConditionTraversal(cnd);
        for (ParserRuleContext innerStmt : t.getInnerStatements()) {
            ParseTree defVar = wrapper.getDefinedVar(innerStmt);
            if (defVar != null) {
                taints.addTaintedVariable(wrapper.getContainer(innerStmt), defVar);
            }
            addedStmts.add(innerStmt);
        }
        return addedStmts;
    }




}
