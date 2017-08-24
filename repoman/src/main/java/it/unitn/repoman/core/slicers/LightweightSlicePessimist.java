package it.unitn.repoman.core.slicers;

import it.unitn.repoman.core.slicers.tainters.BackwardTainterPessimist;
import it.unitn.repoman.core.slicers.tainters.ForwardTainterPessimist;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.LinkedHashSet;
import java.util.Set;

public class LightweightSlicePessimist extends LightweightSlice {

    public LightweightSlicePessimist(ParserRuleContext root, Set<Integer> lines) throws Exception {
        super(root, lines);
    }

    @Override
    protected Set<Integer> makePass(Set<Integer> lines) {
        Set<Integer> matchedLines = new LinkedHashSet<>();
        Set<ParserRuleContext> statements = new LinkedHashSet<>();
        for (ParserRuleContext stmt : collectables) {
            if (lines.contains(stmt.getStart().getLine())) {
                statements.add(stmt);
            }
        }

        ForwardTainterPessimist forwardTainter = new ForwardTainterPessimist(collectables, statements);
        matchedLines.addAll(forwardTainter.getCollectedLines());

        BackwardTainterPessimist backwardTainter = new BackwardTainterPessimist(collectables, statements);
        matchedLines.addAll(backwardTainter.getCollectedLines());

        return matchedLines;
    }

}
