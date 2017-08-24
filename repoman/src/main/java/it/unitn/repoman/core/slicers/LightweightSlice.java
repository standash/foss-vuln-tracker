package it.unitn.repoman.core.slicers;

import java.util.*;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.lang.traversals.generic.MethodCallTraversal;
import it.unitn.repoman.core.lang.traversals.generic.MethodDeclarationTraversal;
import it.unitn.repoman.core.slicers.tainters.BackwardTainter;
import it.unitn.repoman.core.slicers.tainters.ForwardTainter;
import it.unitn.repoman.core.slicers.tainters.TaintedVariableSet;
import org.antlr.v4.runtime.ParserRuleContext;
import it.unitn.repoman.core.lang.traversals.generic.StatementMappingTraversal;
import org.antlr.v4.runtime.tree.ParseTree;

public class LightweightSlice {

	protected final ParserRuleContext root;
	protected final Set<Integer> selectedLines = new LinkedHashSet<>();
    protected final Set<ParserRuleContext> collectables;
    protected final Set<ParserRuleContext> methodDeclarations;

	public LightweightSlice(ParserRuleContext root, Set<Integer> lines) throws Exception {
		this.root = root;
		StatementMappingTraversal statementGetter = new StatementMappingTraversal();
		statementGetter.traverse(this.root);

		collectables = statementGetter.getStatements();
        methodDeclarations = statementGetter.getMethodDeclarations();

        Set<Integer> toAdd = new LinkedHashSet<>();
        toAdd.addAll(makePass(lines));

        selectedLines.addAll(toAdd);
	}

	protected Set<Integer> makePass(Set<Integer> lines) {
        Set<Integer> matchedLines = new LinkedHashSet<>();
        Set<ParserRuleContext> statements = new LinkedHashSet<>();
        for (ParserRuleContext stmt : collectables) {
            if (lines.contains(stmt.getStart().getLine())) {
                statements.add(stmt);
            }
        }

        ForwardTainter forwardTainter = new ForwardTainter(collectables, statements);
        matchedLines.addAll(forwardTainter.getCollectedLines());

        BackwardTainter backwardTainter = new BackwardTainter(collectables, statements);
        matchedLines.addAll(backwardTainter.getCollectedLines());

		/* TODO: Experimental
        Set<Integer> inter = propagrateForwardTaint(collectables, methodDeclarations, forwardTainter);
        matchedLines.addAll(inter);
        */
        return matchedLines;
    }

    // TODO: Experimental
	protected Set<Integer> propagrateForwardTaint(Set<ParserRuleContext> collectables, Set<ParserRuleContext> methodDeclarations, ForwardTainter forwardTainter) {

        Set<Integer> collected = new LinkedHashSet<>();

        // get intra-procedural tainted variables
        TaintedVariableSet taintedVars = forwardTainter.getTaintedVars();

        // get intra-procedural tainted method calls
        Set<ParserRuleContext> taintedMethodCalls = forwardTainter.getTaintedMethodCalls();

        // match a tainted method call to the corresponding method declaration
        for (ParserRuleContext tmc : taintedMethodCalls) {
            MethodCallTraversal t1 = LanguageFactory.getWrapper().getMethodCallTraversal(tmc);
            String methodCallName = t1.getMethodName();
            for (ParserRuleContext md : methodDeclarations) {
                MethodDeclarationTraversal t2 = new MethodDeclarationTraversal(md);
                String methodDeclName = t2.getMethodName();
                if (methodCallName.equals(methodDeclName) && t1.getParamsNumber() == t2.getParamsNumber()) {
                    // ---------------- inter-procedural "expansion" goes here  ----------------------------------------
                    // get tainted variables for the method call statement
                    List<ParseTree> vars = forwardTainter.getTaintedVars().getStatementVariables((ParserRuleContext) t1.getMethodContextNode());
                    // keep only tainted parameters
                    List<String> taintedParams = new LinkedList<>();
                    List<String> params = t1.getParamNames();
                    for (int i=0; i<t1.getParamsNumber(); i++) {
                        String param = t1.getParamName(i);
                        boolean match = false;
                        for (ParseTree var : vars) {
                            if (param.equals(var.getText())) {
                                match = true;
                                break;
                            }
                        }
                        if (match) {
                            taintedParams.add(param);
                        }
                        else {
                            taintedParams.add("NOT_TAINTED");
                        }
                    }

                    // match tainted parameters to aliases in the method declaration
                    List<ParseTree> matchedTaintedParams = new LinkedList<>();
                    for (int i=0; i<taintedParams.size(); i++) {
                        String callParam = taintedParams.get(i);
                        String declParam = t2.getParamNames().get(i);
                        if (!callParam.equals("NOT_TAINTED")) {
                            matchedTaintedParams.add(t2.getParamNode(i));
                        }
                    }

                    // taint relevant stuff within the method declaration...
                    ForwardTainter expandedFt = new ForwardTainter(collectables, matchedTaintedParams);
                    collected.addAll(expandedFt.getCollectedLines());
                    break;
                }
            }
        }
        return collected;
    }

	public Set<Integer> getSelectedLines() {
		return this.selectedLines;
	}

}
