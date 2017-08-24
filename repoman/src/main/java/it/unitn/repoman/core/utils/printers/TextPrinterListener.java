package it.unitn.repoman.core.utils.printers;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.slicers.LightweightSlice;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.*;

import java.util.Set;

public class TextPrinterListener implements ParseTreeListener {
    private final Set<Integer> selectedLines;
    private final Parser parser;
    private final StringBuilder builder = new StringBuilder();
    private boolean marked = false;
    private int currentLineNumber = -1;

    public TextPrinterListener(Parser parser, LightweightSlice slice) {
        this.parser = parser;
        this.selectedLines = slice.getSelectedLines();
        builder.append("Selected lines: " + slice.getSelectedLines() + "\n");
        builder.append("#Selected lines: " + slice.getSelectedLines().size() + "\n");
        ParseTreeWalker.DEFAULT.walk(this, LanguageFactory.getRoot());
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        int lineNumber = ctx.getStart().getLine();
        marked = (selectedLines.contains(lineNumber));
        if (lineNumber != currentLineNumber && marked) {
            builder.append("\n");
            currentLineNumber = lineNumber;
            builder.append(lineNumber);
            builder.append(": ");
        }
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        if (marked) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            String text = Trees.getNodeText(node, this.parser);
            if (text.equals("{")) {
                builder.append(Utils.escapeWhitespace(text, false));
            } else if (text.equals(";")) {
                builder.append(Utils.escapeWhitespace(text, false));
            } else if (text.equals("}")) {
                builder.append(" " + Utils.escapeWhitespace(text, false));
            } else {
                if (!text.contains("<EOF>")) {
                    builder.append(Utils.escapeWhitespace(text, false));
                }
            }
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        builder.append(Utils.escapeWhitespace(Trees.getNodeText(node, this.parser), false));
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
