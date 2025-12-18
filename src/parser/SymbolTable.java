package parser;

import java.util.*;

/**
 * Symbol Table for storing and managing tokens and variables
 * Handles scope management and symbol lookup
 */
public class SymbolTable {

    // Store all tokens for Phase 1 output
    private List<TokenEntry> tokens=  new ArrayList<>();

    // Store variables for semantic analysis
    //* stayed forever
    //TODO (kaizen) hashed `varibales Map`
    // private Map<String, VariableInfo> variables;

    // Scope management (for bonus Phase 4)
    private Stack<Map<String, VariableInfo>> scopeStack = new Stack<>();

    public SymbolTable() {
        //TODO(kaizen) hashed variables assignment
        // this.variables = new HashMap<>();
        // this.scopeStack.push(variables); // Global scope
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
    private String normalize(String text) {
        return text
                .replaceAll("\\s+", " ") //TODO (kaizen)
                .replace("\r\n", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .trim();
    }
    /**
     * Print symbol table in formatted style
     */
    public void printSymbolTable() {
        System.out.println("\n========== SYMBOL TABLE ==========");
        System.out.println();
        System.out.println(String.format("%-15s %-10s %-8s %-10s %-10s",
                "NAME", "TYPE", "SCOPE", "LINE", "COLUMN"));
        System.out.println("----------------------------------------------------------------------");
        int count = 1;
        for (TokenEntry entry : tokens) {
            System.out.printf("│ %-4d │ %-19s │ %-22s │ %4d │ %6d │%n",
                    count++, entry.type, normalize(entry.lexeme), entry.line, entry.column);
        }

        System.out.println("└──────┴─────────────────────┴────────────────────────┴──────┴────────┘");
    }

    // ==================== VARIABLE MANAGEMENT ====================
    public boolean isVariableDeclared(String name) {
        return lookupVariable(name) != null;
    }    /**
     * Add a variable to the symbol table
     */
    public void addVariable(String name, String type, int line) {
        Map<String, VariableInfo> currentScope = scopeStack.peek();
        //TODO(kaizen) redeclared variable
        if (currentScope.containsKey(name)) {
            throw new RuntimeException(
                "Semantic Error: Variable '" + name +
                "' redeclared at line " + line
            );
        }   //TODO(kaizen) #change 12

        int currentScopeLevel = getScopeLevel(); // get current scope level
        
        System.out.println("[ Log ] ID = "+ name + "\tin scopeLevel: " + currentScopeLevel);
        currentScope.put(name, new VariableInfo(name, type, line , currentScopeLevel));
        // currentScope.put(name, new VariableInfo(name, type, line));
    }

    /**
     * Mark a variable as used
     */
    public void markVariableUsed(String identifierImage) {
        VariableInfo info = lookupVariable(identifierImage);
        // if (info != null) {
        //     info.used = true;
        // }
        // //TODO (kaizen) variable used before declare
         if (info == null) {
            throw new RuntimeException(
                "Semantic Error: Variable '" + identifierImage +
                "' used before declaration"
            );
        }
        info.used = true;
    }

    /**
     * Lookup a variable in current and parent scopes
     */
    public VariableInfo lookupVariable(String identiferImage) {
        // Search from innermost to outermost scope
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, VariableInfo> scope = scopeStack.get(i);
            if (scope.containsKey(identiferImage)) {
                return scope.get(identiferImage);
            }
            System.out.println("[log] we don't find identifer ( "+ identiferImage + " ) in scope "+ i);
        }
        return null;
    }

    /**
     * Check if variable exists in current scope only
     */

    // TODO (kaizen) this method doesn't used
    public boolean existsInCurrentScope(String name) {
        return scopeStack.peek().containsKey(name);
    }

    // ==================== SCOPE MANAGEMENT ====================

    /**
     * Enter a new scope (for blocks, methods, etc.)
     */
    public void enterScope() {
        System.out.println("[log] Enter scope No " + scopeStack.size());
        scopeStack.push(new HashMap<>());
    }

    /**
     * Exit current scope
     */
    public void exitScope() {
        if (scopeStack.size() > 1) {
            scopeStack.pop();
                    System.out.println("[log] Exit scope No " + scopeStack.size());

        }
    }

    /**
     * Get current scope level (0 = global)
     */
    public int getScopeLevel() {
        int scopeLevel = scopeStack.size() - 1;

        return scopeLevel;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check for unused variables
     */
    public List<String> getUnusedVariables() {
        List<String> unused = new ArrayList<>();

        // for (VariableInfo info : variables.values()) {
        //     if (!info.used) {
        //         unused.add(info.name + " (line " + info.line + ")");
        //     }
        // }

        //TODO (kaizen) replace the previous code with
        for (Map<String, VariableInfo> scope : scopeStack) {
            for (VariableInfo v : scope.values()) {
                if (!v.used) {
                    unused.add(v.name + " (line " + v.line + ")");
                }
            }
        }
        return unused;
    }
    /**
     * Clear all data
     */
    public void clear() {
        tokens.clear();
        // variables.clear(); //TODO (kaizen)
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