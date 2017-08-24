package it.unitn.molerat.repos.utils;

import it.unitn.repoman.core.lang.LanguageFactory;
import it.unitn.repoman.core.lang.parsers.java.JavaBaseListener;
import it.unitn.repoman.core.lang.parsers.java.JavaParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Queue;
import java.util.HashSet;

public class SignatureExtractor extends JavaBaseListener {

    private String className = "";
    private String packageName = "";
    private String currentSignature = "";
    private final Map<String, Set<Integer>> lines = new TreeMap<>();
    private final Queue<ParserRuleContext> modifiers = new LinkedList<>();


    public SignatureExtractor(String fileContents) {
        LanguageFactory.init("java", fileContents);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, LanguageFactory.getRoot());
    }

    @Override
    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        packageName = ctx.getChild(1).getText();
        currentSignature = packageName;
        processLine(ctx);
    }

    @Override
    public void exitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        currentSignature = "";
    }

    @Override
    public void enterClassOrInterfaceModifier(JavaParser.ClassOrInterfaceModifierContext ctx) {
        modifiers.add(ctx);
    }

    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        StringBuilder methodDeclBuilder = new StringBuilder();
        getMethodSignature(ctx,methodDeclBuilder);
        String currentModifiers = retrieveModifiers();

        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(currentModifiers);
        signatureBuilder.append(": ");
        signatureBuilder.append(joinPackageNameWithClassName());
        signatureBuilder.append(".");
        signatureBuilder.append(methodDeclBuilder.toString());
        currentSignature = signatureBuilder.toString();
    }

    private String joinPackageNameWithClassName() {
        StringBuilder builder = new StringBuilder();
        if (!packageName.equals("")) {
            builder.append(packageName);
            builder.append(".");
        }
        builder.append(className);
        return builder.toString();
    }

    @Override
    public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        currentSignature = "";
    }

    @Override
    public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
        StringBuilder constrDeclBuilder = new StringBuilder();
        getConstructorSignature(ctx,constrDeclBuilder);
        String currentModifiers = retrieveModifiers();

        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(currentModifiers);
        signatureBuilder.append(": ");
        signatureBuilder.append(joinPackageNameWithClassName());
        signatureBuilder.append(".");
        signatureBuilder.append(constrDeclBuilder.toString());
        currentSignature = signatureBuilder.toString();

        processLine(ctx);
    }

    @Override
    public void exitConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
        currentSignature = "";
    }

    @Override
    public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        if (!className.equals("")) {
            return;
        }
        className = ctx.getChild(1).getText();
        String currentModifiers = retrieveModifiers();

        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(currentModifiers);
        signatureBuilder.append(": ");
        signatureBuilder.append(joinPackageNameWithClassName());
        currentSignature = signatureBuilder.toString();

        processLine(ctx);
    }


    @Override
    public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        currentSignature = "";
    }

    @Override
    public void enterStatement(JavaParser.StatementContext ctx) {
        processLine(ctx);
    }

    @Override
    public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        String currentModifiers = retrieveModifiers();
        String fieldName = ctx.getChild(1).getChild(0).getChild(0).getText();

        StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(currentModifiers);
        signatureBuilder.append(": ");
        signatureBuilder.append(joinPackageNameWithClassName());
        signatureBuilder.append(".");
        signatureBuilder.append(fieldName);
        currentSignature = signatureBuilder.toString();

        processLine(ctx);
    }

    @Override
    public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        currentSignature = "";
    }

    @Override
    public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        processLine(ctx);
    }

    @Override
    public void enterCatchClause(JavaParser.CatchClauseContext ctx) {
        processLine(ctx);
    }

    private void processLine(ParserRuleContext ctx) {
        int line = ctx.getStart().getLine();
        if (currentSignature != null) {
            Set<Integer> tempLines = lines.get(currentSignature);
            if (tempLines != null) {
                lines.remove(currentSignature);
            } else {
                tempLines = new HashSet<>();
            }
            tempLines.add(line);
            lines.put(currentSignature, tempLines);
        }
    }

    /**
     * Gets a method signature recursively
     * @param method
     * @param builder
     */
    private void getMethodSignature(ParseTree method, StringBuilder builder) {
        int start = (method.getClass().equals(JavaParser.MethodDeclarationContext.class)) ? 1 : 0;
        for (int i=start; i<method.getChildCount(); i++) {
            ParseTree node = method.getChild(i);
            if (node.getClass().equals(TerminalNodeImpl.class)) {
                builder.append(node.getText());
            }
            else if (!node.getClass().equals(JavaParser.MethodBodyContext.class) &&
                        !node.getClass().equals(JavaParser.VariableDeclaratorIdContext.class)) {
                getMethodSignature(node, builder);
            }
        }
    }

    /**
     * Gets a constructor signature recursively
     * @param constructor
     * @param builder
     */
    private void getConstructorSignature(ParseTree constructor, StringBuilder builder) {
        for (int i=0; i < constructor.getChildCount(); i++) {
            ParseTree node = constructor.getChild(i);
            if (node.getClass().equals(TerminalNodeImpl.class)) {
               builder.append(node.getText());
            }
            else if (!node.getClass().equals(JavaParser.ConstructorBodyContext.class) &&
                    !node.getClass().equals(JavaParser.VariableDeclaratorIdContext.class)) {
                getConstructorSignature(node, builder);
            }
        }
    }

    /**
     * Gets the modifier keywords
     * @return
     */
    private String retrieveModifiers() {
        StringBuilder currentModifier = new StringBuilder();
        while (modifiers.size() != 0) {
            ParserRuleContext modifier = modifiers.poll();
            if (!modifier.getText().startsWith("@")) {
                currentModifier.append(modifier.getText() + " ");
            }
        }
        return currentModifier.toString();
    }


    public Map<String, Set<Integer>> getSignaturesWithLines() {
        return this.lines;
    }

}
