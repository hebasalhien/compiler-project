package parser;

import java.util.*;

/**
 * Symbol Table for storing and managing tokens and variables
 * Handles scope management and symbol lookup
 */
public class SymbolTable {

    // Store all tokens for Phase 1 output
    private List<TokenEntry> tokens;

    // Store variables for semantic analysis
    private Map<String, VariableInfo> variables;

    // Scope management (for bonus Phase 4)
    private Stack<Map<String, VariableInfo>> scopeStack;

    public SymbolTable() {
        this.tokens = new ArrayList<>();
        this.variables = new HashMap<>();
        this.scopeStack = new Stack<>();
        this.scopeStack.push(variables); // Global scope
    }

    // ==================== TOKEN MANAGEMENT ====================

    /**
     * Record a token for symbol table display
     */
    public void addToken(Token token, String tokenType) {
        tokens.add(new TokenEntry(tokenType, token.image,
                token.beginLine, token.beginColumn));
    }

    /**
     * Get all recorded tokens
     */
    public List<TokenEntry> getTokens() {
        return tokens;
    }

    /**
     * Print symbol table in formatted style
     */
    public void printSymbolTable() {
        System.out.println("┌──────┬─────────────────────┬──────────────┬──────┬────────┐");
        System.out.println("│ No.  │ Token Type          │ Lexeme       │ Line │ Column │");
        System.out.println("├──────┼─────────────────────┼──────────────┼──────┼────────┤");

        int count = 1;
        for (TokenEntry entry : tokens) {
            System.out.printf("│ %-4d │ %-19s │ %-12s │ %4d │ %6d │%n",
                    count++, entry.type, entry.lexeme, entry.line, entry.column);
        }

        System.out.println("└──────┴─────────────────────┴──────────────┴──────┴────────┘");
    }

    // ==================== VARIABLE MANAGEMENT ====================
    public boolean isVariableDeclared(String name) {
        return variables.containsKey(name);
    }
    /**
     * Add a variable to the symbol table
     */
    public void addVariable(String name, String type, int line) {
        Map<String, VariableInfo> currentScope = scopeStack.peek();
        currentScope.put(name, new VariableInfo(name, type, line));
    }

    /**
     * Mark a variable as used
     */
    public void markVariableUsed(String name) {
        VariableInfo info = lookupVariable(name);
        if (info != null) {
            info.used = true;
        }
    }

    /**
     * Lookup a variable in current and parent scopes
     */
    public VariableInfo lookupVariable(String name) {
        // Search from innermost to outermost scope
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, VariableInfo> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    /**
     * Check if variable exists in current scope only
     */
    public boolean existsInCurrentScope(String name) {
        return scopeStack.peek().containsKey(name);
    }

    /**
     * Get all variables (for unused variable detection)
     */
    public Collection<VariableInfo> getAllVariables() {
        return variables.values();
    }

    // ==================== SCOPE MANAGEMENT ====================

    /**
     * Enter a new scope (for blocks, methods, etc.)
     */
    public void enterScope() {
        scopeStack.push(new HashMap<>());
    }

    /**
     * Exit current scope
     */
    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
        }
    }

    /**
     * Get current scope level (0 = global)
     */
    public int getScopeLevel() {
        return scopeStack.size() - 1;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check for unused variables
     */
    public List<String> getUnusedVariables() {
        List<String> unused = new ArrayList<>();
        for (VariableInfo info : variables.values()) {
            if (!info.used) {
                unused.add(info.name + " (line " + info.line + ")");
            }
        }
        return unused;
    }

    /**
     * Print variable information
     */
    public void printVariables() {
        System.out.println("\n┌─────────────────┬──────────┬──────┬──────┐");
        System.out.println("│ Variable Name   │ Type     │ Line │ Used │");
        System.out.println("├─────────────────┼──────────┼──────┼──────┤");

        for (VariableInfo info : variables.values()) {
            System.out.printf("│ %-15s │ %-8s │ %4d │ %-4s │%n",
                    info.name, info.type, info.line, info.used ? "Yes" : "No");
        }

        System.out.println("└─────────────────┴──────────┴──────┴──────┘");
    }

    /**
     * Clear all data
     */
    public void clear() {
        tokens.clear();
        variables.clear();
        scopeStack.clear();
        scopeStack.push(new HashMap<>());
    }

    // ==================== INNER CLASSES ====================

    /**
     * Represents a token entry in the symbol table
     */
    public static class TokenEntry {
        public String type;
        public String lexeme;
        public int line;
        public int column;

        public TokenEntry(String type, String lexeme, int line, int column) {
            this.type = type;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }
    }

    /**
     * Represents a variable in the symbol table
     */
    public static class VariableInfo {
        public String name;
        public String type;
        public int line;
        public boolean used;
        public int scopeLevel;

        public VariableInfo(String name, String type, int line) {
            this.name = name;
            this.type = type;
            this.line = line;
            this.used = false;
            this.scopeLevel = 0;
        }

        public VariableInfo(String name, String type, int line, int scopeLevel) {
            this.name = name;
            this.type = type;
            this.line = line;
            this.used = false;
            this.scopeLevel = scopeLevel;
        }
    }
}