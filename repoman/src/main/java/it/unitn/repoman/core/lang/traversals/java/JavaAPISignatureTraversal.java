package it.unitn.repoman.core.lang.traversals.java;

import java.util.HashSet;
import java.util.Set;

import it.unitn.repoman.core.lang.traversals.generic.DFSTraversal;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import it.unitn.repoman.core.lang.parsers.java.JavaParser;

public class JavaAPISignatureTraversal extends DFSTraversal {
	 protected final Set<String> signatures = new HashSet<>();
	
	 public JavaAPISignatureTraversal(ParseTree node) {
		 traverse(node);
	 }
	 
	 @Override
	 public void onEnterNode(ParseTree node) {
		if (wrapper.isMethodDeclaration(node)) {
			ParserRuleContext method = (ParserRuleContext) node;
			this.signatures.add(getMethodSignature(method));
		}
	 }
	 
	 public String getMethodSignature(ParserRuleContext method) {
			StringBuilder builder = new StringBuilder();
			if (wrapper.isMethodDeclaration(method)) {
				ParserRuleContext declRoot = method.getParent().getParent();
				for (int i=0; i<declRoot.getChildCount(); i++) {
					ParseTree node = declRoot.getChild(i);
					if (node.getClass().equals(JavaParser.ModifierContext.class)) {
						builder.append(node.getText());
					}
				}
				for (int i=0; i<method.getChildCount(); i++) {
					ParseTree node = method.getChild(i);
					if (wrapper.isTerminal(node) || node.getClass().equals(JavaParser.TypeTypeContext.class)
							|| node.getClass().equals(JavaParser.FormalParametersContext.class)) {
						builder.append(node.getText());
					}
				}
				return refineSignature(builder.toString());
			}
			return "";
	 }
	 
	 public boolean methodSignatureExists(String signature) {
		 signature = refineSignature(signature);
		 for (String s : signatures) {
			 if (s.equals(signature)) {
				 return true;
			 }
		 }
		 return false;
	 }
	 

	 public boolean isPublicMethod(String signature) {
		 signature = refineSignature(signature);
		 return signature.startsWith("public");
	 }
	 
	 public boolean isProtectedMethod(String signature) {
		 signature = refineSignature(signature);
		 return signature.startsWith("protected");
	 }

	 public boolean isPrivateMethod(String signature) {
		 signature = refineSignature(signature);
		 return signature.startsWith("private");
	 }
	 
	 protected String refineSignature(String signature) {
		 int closingBracketPosition = signature.indexOf(")");
		 if (closingBracketPosition != -1) {
			 return signature.replace(signature.substring(signature.indexOf(")")), ")").replace("@Override", "");
		 }
		 return signature;
	 }
	 
	 public int getSignaturesCount() {
		 return this.signatures.size();
	 }
	 
	 public Set<String> getPublicSignatures() {
		 Set<String> s = new HashSet<>();
		 for (String signature : this.signatures) {
			 if (this.isPublicMethod(signature)) {
				 s.add(signature);
			 }
		 }
		 return s; 
	 }
	 
	 public Set<String> getProtectedSignatures() {
		 Set<String> s = new HashSet<>();
		 for (String signature : this.signatures) {
			 if (this.isProtectedMethod(signature)) {
				 s.add(signature);
			 }
		 }
		 return s; 	
	 }
	 
	 public Set<String> getPrivateSignatures() {
		 Set<String> s = new HashSet<>();
		 for (String signature : this.signatures) {
			 if (this.isPrivateMethod(signature)) {
				 s.add(signature);
			 }
		 }
		 return s; 	
	 }

     public Set<String> getSignatures() {
         return this.signatures;
     }
	 
	 
	 public int getPublicSignaturesCount() {
		 return this.getPublicSignatures().size();
	 }
}
