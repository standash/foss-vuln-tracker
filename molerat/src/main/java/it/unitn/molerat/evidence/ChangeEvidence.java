package it.unitn.molerat.evidence;

public class ChangeEvidence extends GenericEvidence {

    private final boolean removed;

    public ChangeEvidence(String file, String commit, String container, boolean removed) {
        super(file, commit, container);
        this.removed = removed;
    }

    public boolean isMethodOrConstructor() {
        return this.container.contains("(") && this.container.contains(")");
    }

    public boolean isAbsentInFix() {
        return removed;
    }

    public String getAccessModifier() {
        if (this.container.startsWith("public")) {
            return "public";
        }
        else if (this.container.startsWith("protected")) {
            return "protected";
        }
        else if (this.container.startsWith("private")) {
            return "private";
        }
        return "";
    }

    public boolean isPublicMethodOrConstructor() {
       return this.isMethodOrConstructor() && this.getAccessModifier().equals("public");
    }

    public boolean isProtectedMethodOrConstructor() {
        return this.isMethodOrConstructor() && this.getAccessModifier().equals("protected");
    }

    public boolean isPrivateMethodOrConstructor() {
        return this.isMethodOrConstructor() && this.getAccessModifier().equals("private");
    }

}
