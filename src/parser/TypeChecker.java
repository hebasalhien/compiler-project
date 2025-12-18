package parser;

import java.util.*;

/**
 * Type Checker for semantic analysis
 * Performs type checking for expressions and variable assignments
 */
public class TypeChecker {

    private SymbolTable symbolTable;
    private List<String> errors;
    private List<String> warnings;

    // Type compatibility matrix
    private static final Map<String, Set<String>> COMPATIBLE_TYPES = new HashMap<>();

    static {
        // Numeric type hierarchy (implicit widening conversions)
        COMPATIBLE_TYPES.put("byte", new HashSet<>(Arrays.asList("byte", "short", "int", "long", "float", "double")));
        COMPATIBLE_TYPES.put("short", new HashSet<>(Arrays.asList("short", "int", "long", "float", "double")));
        COMPATIBLE_TYPES.put("int", new HashSet<>(Arrays.asList("int", "long", "float", "double")));
        COMPATIBLE_TYPES.put("long", new HashSet<>(Arrays.asList("long", "float", "double")));
        COMPATIBLE_TYPES.put("float", new HashSet<>(Arrays.asList("float", "double")));
        COMPATIBLE_TYPES.put("double", new HashSet<>(Arrays.asList("double")));
        COMPATIBLE_TYPES.put("char", new HashSet<>(Arrays.asList("char", "int", "long", "float", "double")));

        // Boolean (no implicit conversion)
        COMPATIBLE_TYPES.put("boolean", new HashSet<>(Arrays.asList("boolean")));

        // String (no implicit conversion)
        COMPATIBLE_TYPES.put("String", new HashSet<>(Arrays.asList("String")));
    }

    public TypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Check if a value of fromType can be assigned to a variable of toType
     */
    public boolean isAssignmentCompatible(String fromType, String toType) {
        if (fromType == null || toType == null) {
            return false;
        }

        // Exact match
        if (fromType.equals(toType)) {
            return true;
        }

        // Check if implicit conversion is allowed
        Set<String> compatibleTypes = COMPATIBLE_TYPES.get(fromType);
        if (compatibleTypes != null) {
            return compatibleTypes.contains(toType);
        }

        return false;
    }

    /**
     * Get the type of an expression from AST node
     */
    public String getExpressionType(ASTNode node) {
        if (node == null) {
            return null;
        }

        if (node instanceof LiteralNode) {
            return getLiteralType((LiteralNode) node);
        } else if (node instanceof IdentifierNode) {
            return getIdentifierType((IdentifierNode) node);
        } else if (node instanceof BinaryOpNode) {
            return getBinaryOpType((BinaryOpNode) node);
        } else if (node instanceof UnaryOpNode) {
            return getUnaryOpType((UnaryOpNode) node);
        } else if (node instanceof MethodCallNode) {
            return "unknown"; // Would need method signature info
        } else if (node instanceof MemberAccessNode) {
            return "unknown"; // Would need type inference
        }

        return "unknown";
    }

    /**
     * Get type of literal
     */
    private String getLiteralType(LiteralNode node) {
        // Access the type field using reflection or make it public
        // For now, we'll infer from the node structure
        try {
            java.lang.reflect.Field typeField = LiteralNode.class.getDeclaredField("type");
            typeField.setAccessible(true);
            return (String) typeField.get(node);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get type of identifier (variable)
     */
    private String getIdentifierType(IdentifierNode node) {
        String name = node.getName();
        SymbolTable.VariableInfo varInfo = symbolTable.lookupVariable(name);

        if (varInfo != null) {
            return varInfo.type;
        }

        return "unknown";
    }

    /**
     * Get result type of binary operation
     */
    private String getBinaryOpType(BinaryOpNode node) {
        try {
            java.lang.reflect.Field opField = BinaryOpNode.class.getDeclaredField("operator");
            java.lang.reflect.Field leftField = BinaryOpNode.class.getDeclaredField("left");
            java.lang.reflect.Field rightField = BinaryOpNode.class.getDeclaredField("right");

            opField.setAccessible(true);
            leftField.setAccessible(true);
            rightField.setAccessible(true);

            String operator = (String) opField.get(node);
            ASTNode left = (ASTNode) leftField.get(node);
            ASTNode right = (ASTNode) rightField.get(node);

            String leftType = getExpressionType(left);
            String rightType = getExpressionType(right);

            // Relational and equality operators return boolean
            if (operator.equals("==") || operator.equals("!=") ||
                    operator.equals("<") || operator.equals(">") ||
                    operator.equals("<=") || operator.equals(">=")) {
                return "boolean";
            }

            // Logical operators return boolean
            if (operator.equals("&&") || operator.equals("||")) {
                return "boolean";
            }

            // Arithmetic operators: return wider type
            return getWiderType(leftType, rightType);

        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get result type of unary operation
     */
    private String getUnaryOpType(UnaryOpNode node) {
        try {
            java.lang.reflect.Field opField = UnaryOpNode.class.getDeclaredField("operator");
            java.lang.reflect.Field operandField = UnaryOpNode.class.getDeclaredField("operand");

            opField.setAccessible(true);
            operandField.setAccessible(true);

            String operator = (String) opField.get(node);
            ASTNode operand = (ASTNode) operandField.get(node);

            String operandType = getExpressionType(operand);

            // Logical NOT returns boolean
            if (operator.equals("!")) {
                return "boolean";
            }

            // Unary minus, ++, -- return same type as operand
            return operandType;

        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Get wider type for numeric promotion
     */
    private String getWiderType(String type1, String type2) {
        if (type1 == null || type2 == null) {
            return "unknown";
        }

        // Order: byte < short < int < long < float < double
        String[] typeOrder = {"byte", "short", "char", "int", "long", "float", "double"};

        int index1 = -1, index2 = -1;
        for (int i = 0; i < typeOrder.length; i++) {
            if (typeOrder[i].equals(type1)) index1 = i;
            if (typeOrder[i].equals(type2)) index2 = i;
        }

        if (index1 == -1 || index2 == -1) {
            return "unknown";
        }

        return typeOrder[Math.max(index1, index2)];
    }

    /**
     * Check assignment statement for type compatibility
     */
    public void checkAssignment(String varName, ASTNode expression, int line) {
        SymbolTable.VariableInfo varInfo = symbolTable.lookupVariable(varName);

        if (varInfo == null) {
            errors.add("Line " + line + ": Variable '" + varName + "' not declared");
            return;
        }

        String varType = varInfo.type;
        String exprType = getExpressionType(expression);

        if (exprType.equals("unknown")) {
            warnings.add("Line " + line + ": Cannot determine type of expression in assignment to '" + varName + "'");
            return;
        }

        if (!isAssignmentCompatible(exprType, varType)) {
            errors.add("Line " + line + ": Type mismatch: cannot assign " + exprType + " to " + varType + " in variable '" + varName + "'");
        }
    }

    /**
     * Check binary operation for type compatibility
     */
    public void checkBinaryOperation(String operator, ASTNode left, ASTNode right, int line) {
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);

        if (leftType.equals("unknown") || rightType.equals("unknown")) {
            return; // Skip if types unknown
        }

        // Check relational operators require comparable types
        if (operator.equals("<") || operator.equals(">") ||
                operator.equals("<=") || operator.equals(">=")) {
            if (!isNumericType(leftType) || !isNumericType(rightType)) {
                errors.add("Line " + line + ": Relational operator '" + operator + "' requires numeric operands, got " + leftType + " and " + rightType);
            }
        }

        // Check equality operators
        if (operator.equals("==") || operator.equals("!=")) {
            if (!leftType.equals(rightType) && !isAssignmentCompatible(leftType, rightType) && !isAssignmentCompatible(rightType, leftType)) {
                warnings.add("Line " + line + ": Comparing incompatible types: " + leftType + " and " + rightType);
            }
        }

        // Check logical operators require boolean
        if (operator.equals("&&") || operator.equals("||")) {
            if (!leftType.equals("boolean")) {
                errors.add("Line " + line + ": Logical operator '" + operator + "' requires boolean operands, left operand is " + leftType);
            }
            if (!rightType.equals("boolean")) {
                errors.add("Line " + line + ": Logical operator '" + operator + "' requires boolean operands, right operand is " + rightType);
            }
        }

        // Check arithmetic operators require numeric types
        if (operator.equals("+") || operator.equals("-") ||
                operator.equals("*") || operator.equals("/") || operator.equals("%")) {
            if (!isNumericType(leftType) && !leftType.equals("String")) {
                errors.add("Line " + line + ": Arithmetic operator '" + operator + "' requires numeric operands, left operand is " + leftType);
            }
            if (!isNumericType(rightType) && !rightType.equals("String")) {
                errors.add("Line " + line + ": Arithmetic operator '" + operator + "' requires numeric operands, right operand is " + rightType);
            }
        }
    }

    /**
     * Check if condition is boolean type
     */
    public void checkCondition(ASTNode condition, String statementType, int line) {
        String condType = getExpressionType(condition);

        if (condType.equals("unknown")) {
            warnings.add("Line " + line + ": Cannot determine type of " + statementType + " condition");
            return;
        }

        if (!condType.equals("boolean")) {
            errors.add("Line " + line + ": " + statementType + " condition must be boolean, got " + condType);
        }
    }

    /**
     * Check if type is numeric
     */
    private boolean isNumericType(String type) {
        return type.equals("byte") || type.equals("short") || type.equals("int") ||
                type.equals("long") || type.equals("float") || type.equals("double") ||
                type.equals("char");
    }

    /**
     * Analyze the entire AST for type errors
     */
    public void analyze(ASTNode root) {
        analyzeNode(root);
    }

    /**
     * Recursively analyze AST nodes
     */
    private void analyzeNode(ASTNode node) {
        if (node == null) {
            return;
        }

        try {
            if (node instanceof AssignmentNode) {
                analyzeAssignment((AssignmentNode) node);
            } else if (node instanceof IfNode) {
                analyzeIf((IfNode) node);
            } else if (node instanceof WhileNode) {
                analyzeWhile((WhileNode) node);
            } else if (node instanceof DoWhileNode) {
                analyzeDoWhile((DoWhileNode) node);
            } else if (node instanceof ForNode) {
                analyzeFor((ForNode) node);
            } else if (node instanceof BinaryOpNode) {
                analyzeBinaryOp((BinaryOpNode) node);
            } else if (node instanceof BlockNode) {
                analyzeBlock((BlockNode) node);
            } else if (node instanceof MethodNode) {
                analyzeMethod((MethodNode) node);
            } else if (node instanceof ClassNode) {
                analyzeClass((ClassNode) node);
            } else if (node instanceof ProgrameNode) {
                analyzeProgram((ProgrameNode) node);
            } else if (node instanceof VariableDeclarationNode) {
                analyzeVariableDeclaration((VariableDeclarationNode) node);
            }
        } catch (Exception e) {
            warnings.add("Error analyzing node: " + e.getMessage());
        }
    }

    private void analyzeAssignment(AssignmentNode node) throws Exception {
        java.lang.reflect.Field varField = AssignmentNode.class.getDeclaredField("variableName");
        java.lang.reflect.Field exprField = AssignmentNode.class.getDeclaredField("expression");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        varField.setAccessible(true);
        exprField.setAccessible(true);
        lineField.setAccessible(true);

        String varName = (String) varField.get(node);
        ASTNode expr = (ASTNode) exprField.get(node);
        int line = (int) lineField.get(node);

        checkAssignment(varName, expr, line);
        analyzeNode(expr);
    }

    private void analyzeIf(IfNode node) throws Exception {
        java.lang.reflect.Field condField = IfNode.class.getDeclaredField("condition");
        java.lang.reflect.Field thenField = IfNode.class.getDeclaredField("thenStatement");
        java.lang.reflect.Field elseField = IfNode.class.getDeclaredField("elseStatement");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        condField.setAccessible(true);
        thenField.setAccessible(true);
        elseField.setAccessible(true);
        lineField.setAccessible(true);

        ASTNode condition = (ASTNode) condField.get(node);
        ASTNode thenStmt = (ASTNode) thenField.get(node);
        ASTNode elseStmt = (ASTNode) elseField.get(node);
        int line = (int) lineField.get(node);

        checkCondition(condition, "if", line);
        analyzeNode(condition);
        analyzeNode(thenStmt);
        if (elseStmt != null) {
            analyzeNode(elseStmt);
        }
    }

    private void analyzeWhile(WhileNode node) throws Exception {
        java.lang.reflect.Field condField = WhileNode.class.getDeclaredField("condition");
        java.lang.reflect.Field bodyField = WhileNode.class.getDeclaredField("body");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        condField.setAccessible(true);
        bodyField.setAccessible(true);
        lineField.setAccessible(true);

        ASTNode condition = (ASTNode) condField.get(node);
        ASTNode body = (ASTNode) bodyField.get(node);
        int line = (int) lineField.get(node);

        checkCondition(condition, "while", line);
        analyzeNode(condition);
        analyzeNode(body);
    }

    private void analyzeDoWhile(DoWhileNode node) throws Exception {
        java.lang.reflect.Field condField = DoWhileNode.class.getDeclaredField("condition");
        java.lang.reflect.Field bodyField = DoWhileNode.class.getDeclaredField("body");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        condField.setAccessible(true);
        bodyField.setAccessible(true);
        lineField.setAccessible(true);

        ASTNode condition = (ASTNode) condField.get(node);
        ASTNode body = (ASTNode) bodyField.get(node);
        int line = (int) lineField.get(node);

        checkCondition(condition, "do-while", line);
        analyzeNode(condition);
        analyzeNode(body);
    }

    private void analyzeFor(ForNode node) throws Exception {
        java.lang.reflect.Field initField = ForNode.class.getDeclaredField("init");
        java.lang.reflect.Field condField = ForNode.class.getDeclaredField("condition");
        java.lang.reflect.Field updateField = ForNode.class.getDeclaredField("update");
        java.lang.reflect.Field bodyField = ForNode.class.getDeclaredField("body");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        initField.setAccessible(true);
        condField.setAccessible(true);
        updateField.setAccessible(true);
        bodyField.setAccessible(true);
        lineField.setAccessible(true);

        ASTNode init = (ASTNode) initField.get(node);
        ASTNode condition = (ASTNode) condField.get(node);
        ASTNode update = (ASTNode) updateField.get(node);
        ASTNode body = (ASTNode) bodyField.get(node);
        int line = (int) lineField.get(node);

        analyzeNode(init);
        if (condition != null) {
            checkCondition(condition, "for", line);
            analyzeNode(condition);
        }
        analyzeNode(update);
        analyzeNode(body);
    }

    private void analyzeBinaryOp(BinaryOpNode node) throws Exception {
        java.lang.reflect.Field opField = BinaryOpNode.class.getDeclaredField("operator");
        java.lang.reflect.Field leftField = BinaryOpNode.class.getDeclaredField("left");
        java.lang.reflect.Field rightField = BinaryOpNode.class.getDeclaredField("right");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        opField.setAccessible(true);
        leftField.setAccessible(true);
        rightField.setAccessible(true);
        lineField.setAccessible(true);

        String operator = (String) opField.get(node);
        ASTNode left = (ASTNode) leftField.get(node);
        ASTNode right = (ASTNode) rightField.get(node);
        int line = (int) lineField.get(node);

        checkBinaryOperation(operator, left, right, line);
        analyzeNode(left);
        analyzeNode(right);
    }

    private void analyzeBlock(BlockNode node) throws Exception {
        java.lang.reflect.Field stmtsField = BlockNode.class.getDeclaredField("statements");
        stmtsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ASTNode> statements = (List<ASTNode>) stmtsField.get(node);

        for (ASTNode stmt : statements) {
            analyzeNode(stmt);
        }
    }

    private void analyzeMethod(MethodNode node) throws Exception {
        java.lang.reflect.Field stmtsField = MethodNode.class.getDeclaredField("statements");
        stmtsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ASTNode> statements = (List<ASTNode>) stmtsField.get(node);

        for (ASTNode stmt : statements) {
            analyzeNode(stmt);
        }
    }

    private void analyzeClass(ClassNode node) throws Exception {
        java.lang.reflect.Field membersField = ClassNode.class.getDeclaredField("members");
        membersField.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ASTNode> members = (List<ASTNode>) membersField.get(node);

        for (ASTNode member : members) {
            analyzeNode(member);
        }
    }

    private void analyzeProgram(ProgrameNode node) throws Exception {
        java.lang.reflect.Field classesField = ProgrameNode.class.getDeclaredField("classes");
        classesField.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ASTNode> classes = (List<ASTNode>) classesField.get(node);

        for (ASTNode classNode : classes) {
            analyzeNode(classNode);
        }
    }

    private void analyzeVariableDeclaration(VariableDeclarationNode node) throws Exception {
        java.lang.reflect.Field initField = VariableDeclarationNode.class.getDeclaredField("initializer");
        java.lang.reflect.Field typeField = VariableDeclarationNode.class.getDeclaredField("type");
        java.lang.reflect.Field nameField = VariableDeclarationNode.class.getDeclaredField("name");
        java.lang.reflect.Field lineField = ASTNode.class.getDeclaredField("line");

        initField.setAccessible(true);
        typeField.setAccessible(true);
        nameField.setAccessible(true);
        lineField.setAccessible(true);

        ASTNode initializer = (ASTNode) initField.get(node);
        String type = (String) typeField.get(node);
        String name = (String) nameField.get(node);
        int line = (int) lineField.get(node);

        if (initializer != null) {
            String initType = getExpressionType(initializer);
            if (!initType.equals("unknown") && !isAssignmentCompatible(initType, type)) {
                errors.add("Line " + line + ": Type mismatch in initialization of '" + name + "': cannot assign " + initType + " to " + type);
            }
            analyzeNode(initializer);
        }
    }

    /**
     * Print type checking results
     */
    public void printResults() {
        System.out.println("TYPE CHECKING RESULTS");
        System.out.println("========================================");

        if (errors.isEmpty() && warnings.isEmpty()) {
            System.out.println(" No type errors found!");
        } else {
            if (!errors.isEmpty()) {
                System.out.println("\n TYPE ERRORS:");
                for (String error : errors) {
                    System.out.println("  " + error);
                }
            }

            if (!warnings.isEmpty()) {
                System.out.println("\n⚠ TYPE WARNINGS:");
                for (String warning : warnings) {
                    System.out.println("  " + warning);
                }
            }
        }
        System.out.println();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}