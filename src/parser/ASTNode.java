package parser;
import java.util.*;
public abstract  class ASTNode {

    protected int line;
    protected  int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public abstract void print(int indent);

    protected void printIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
    }

    public abstract void printTree(String prefix, boolean isLast);

    protected void tree(String prefix, boolean isLast, String text) {
        System.out.print(prefix);
        System.out.print(isLast ? "└── " : "├── ");
        System.out.println(text);
    }
}

class ProgrameNode extends ASTNode {
    private List<ASTNode> classes;

    public ProgrameNode() {
        super(0, 0);
        this.classes = new ArrayList<>();
    }

    public void addClass(ASTNode classNode) {
        this.classes.add(classNode);
    }

    public List<ASTNode> getClasses() {
        return classes;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Program");

        for (ASTNode classNode : classes) {
            classNode.print(indent + 2);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Program");
        String p = prefix + (isLast ? "    " : "│   ");

        for (int i = 0; i < classes.size(); i++) {
            classes.get(i).printTree(p, i == classes.size() - 1);
        }
    }
}

class ClassNode extends ASTNode {
    private String modifiers;
    private String name;
    private List<ASTNode> members = new ArrayList<>();

    public ClassNode(String name,String modifiers,int line, int column) {
        super(line, column);
        this.name=name;
        this.modifiers=modifiers;
    }

    public void addMember(ASTNode member) {
        members.add(member);
    }

    public String getName() {
        return name;
    }

    public String getModifiers() {
        return modifiers;
    }

    public List<ASTNode> getMembers() {
        return members;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        if (modifiers != null && !modifiers.isEmpty()) {
            System.out.println("ClassDeclaration: " + modifiers + " " + name + " [Line " + line + "]");
        } else {
            System.out.println("ClassDeclaration: " + name + " [Line " + line + "]");
        }
        for (ASTNode member : members) {
            member.print(indent + 1);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        if (modifiers != null && !modifiers.isEmpty()) {
            tree(prefix, isLast, "ClassDeclaration: " +modifiers+" " +name+ " [Line " + line + "]");
        } else {
            tree(prefix, isLast, "ClassDeclaration: " + name+ " [Line " + line + "]");
        }
        String p = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < members.size(); i++) {
            members.get(i).printTree(p, i == members.size() - 1);
        }
    }
}

class MethodNode extends ASTNode {
    private String modifiers;
    private String returnType;
    private String name;
    private  List<ASTNode> parameters ;
    private  List<ASTNode> statements ;

    public MethodNode(String modifiers,String returnType,String name,int line, int column) {
        super(line, column);
        this.modifiers=modifiers;
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
        this.statements = new ArrayList<>();
    }

    public void setParameter(List<ASTNode> parameter) {
        this.parameters=parameter;
    }

    public void setStatement(List<ASTNode> statement) {
        this.statements=statement;
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getModifiers() {
        return modifiers;
    }

    public List<ASTNode> getParameters() {
        return parameters;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        if (modifiers != null && !modifiers.isEmpty()) {
            System.out.println("MethodDeclaration: " + modifiers + " " + returnType + " " + name + " [Line " + line + "]");
        } else {
            System.out.println("MethodDeclaration: " + returnType + " " + name + " [Line " + line + "]");
        }

        if (!parameters.isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Parameters:");
            for (ASTNode param : parameters) {
                param.print(indent + 2);
            }
        }

        if (!statements.isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Body:");
            for (ASTNode stmt : statements) {
                stmt.print(indent + 2);
            }
        }
    }
    @Override
    public void printTree(String prefix, boolean isLast) {
        if (modifiers != null && !modifiers.isEmpty()) {
            tree(prefix,isLast,"MethodDeclaration: " + modifiers + " " + returnType + " " + name + " [Line " + line + "]");
        } else {
            tree(prefix,isLast,"MethodDeclaration: " + returnType + " " + name + " [Line " + line + "]");
        }
        String p = prefix + (isLast ? "    " : "│   ");

        if (!parameters.isEmpty()) {
            tree(p, false, "Parameters");
            for (int i = 0; i < parameters.size(); i++) {
                parameters.get(i).printTree(p + "│   ", i == parameters.size() - 1);
            }
        }

        if (!statements.isEmpty()) {
            tree(p, true, "Body");
            for (int i = 0; i < statements.size(); i++) {
                statements.get(i).printTree(p + "    ", i == statements.size() - 1);
            }
        }
    }
}

class ParameterNode extends ASTNode {
    private String type;
    private String name;
    private boolean isArray;

    public ParameterNode(String type,String name,boolean isArray,int line, int column) {
        super(line, column);
        this.name=name;
        this.type=type;
        this.isArray=isArray;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        if (isArray) {
            System.out.println("Parameter: " + type + "[] " + name);
        } else {
            System.out.println("Parameter: " + type + " " + name);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        String arr = isArray ? "[]" : "";
        tree(prefix, isLast, "Parameter: " + type + arr + " " + name);
    }
}

class FieldNode extends ASTNode {
    private String modifiers;
    private String type;
    private String name;
    private boolean isArray;

    public FieldNode(String modifiers, String type, String name, boolean isArray, int line, int column) {
        super(line, column);
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.isArray = isArray;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getModifiers() {
        return modifiers;
    }

    public boolean isArray() {
        return isArray;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        String arrayStr = isArray ? "[]" : "";
        if (modifiers != null && !modifiers.isEmpty()) {
            System.out.println("Field: " + modifiers + " " + type + arrayStr + " " + name + " [Line " + line + "]");
        } else {
            System.out.println("Field: " + type + arrayStr + " " + name + " [Line " + line + "]");
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        String arr = isArray ? "[]" : "";
        tree(prefix, isLast, "Field: " + type + arr + " " + name + " [Line " + line + "]");
    }
}

class VariableDeclarationNode extends ASTNode{
    private String type;
    private String name;
    private boolean isArray;
    private ASTNode initializer;

    public VariableDeclarationNode(String type,String name,boolean isArray,ASTNode initializer,int line, int column) {
        super(line, column);
        this.name = name;
        this.type=type;
        this.isArray=isArray;
        this.initializer = initializer;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public ASTNode getInitializer() {
        return initializer;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        String arrayStr = isArray ? "[]" : "";
        System.out.println("VariableDeclaration: " + type + arrayStr + " " + name + " [Line " + line + "]");
        if (initializer != null) {
            printIndent(indent + 1);
            System.out.println("Initializer:");
            initializer.print(indent + 2);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "VariableDeclaration: " + type + " " + name+ " [Line " + line + "]");
        if (initializer != null) {
            initializer.printTree(prefix + (isLast ? "    " : "│   "), true);
        }
    }
}

class AssignmentNode extends ASTNode {
    private String variableName;
    private ASTNode expression;

    public AssignmentNode(String variableName, ASTNode expression, int line, int column) {
        super(line, column);
        this.variableName = variableName;
        this.expression = expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public ASTNode getExpression() {
        return expression;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Assignment: " + variableName + " [Line " + line + "]");
        if (expression != null) {
            printIndent(indent + 1);
            System.out.println("Expression:");
            expression.print(indent + 2);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Assignment: " + variableName+ " [Line " + line + "]");
        if (expression != null) {
            expression.printTree(prefix + (isLast ? "    " : "│   "), true);
        }
    }
}

class IfNode extends ASTNode {
    private ASTNode condition;
    private ASTNode thenStatement;
    private ASTNode elseStatement;

    public IfNode(ASTNode condition, ASTNode thenStatement, ASTNode elseStatement, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getThenStatement() {
        return thenStatement;
    }

    public ASTNode getElseStatement() {
        return elseStatement;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("IfStatement [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Condition:");
        condition.print(indent + 2);

        printIndent(indent + 1);
        System.out.println("Then:");
        thenStatement.print(indent + 2);

        if (elseStatement != null) {
            printIndent(indent + 1);
            System.out.println("Else:");
            elseStatement.print(indent + 2);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "IfStatement [line "+line+ " ]");
        String p = prefix + (isLast ? "    " : "│   ");

        tree(p, false, "Condition");
        condition.printTree(p + "│   ", true);

        tree(p, elseStatement == null, "Then");
        thenStatement.printTree(p + "    ", true);

        if (elseStatement != null) {
            tree(p, true, "Else");
            elseStatement.printTree(p + "    ", true);
        }
    }
}

class WhileNode extends ASTNode {
    private ASTNode condition;
    private ASTNode body;

    public WhileNode(ASTNode condition, ASTNode body, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.body = body;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getBody() {
        return body;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("WhileLoop [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Condition:");
        condition.print(indent + 2);

        printIndent(indent + 1);
        System.out.println("Body:");
        body.print(indent + 2);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "WhileLoop [Line " + line + "]");
        String p = prefix + (isLast ? "    " : "│   ");

        tree(p, false, "Condition");
        condition.printTree(p + "│   ", true);

        tree(p, true, "Body");
        body.printTree(p + "    ", true);
    }
}

class DoWhileNode extends ASTNode {
    private ASTNode condition;
    private ASTNode body;

    public DoWhileNode(ASTNode condition, ASTNode body, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.body = body;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getBody() {
        return body;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("DoWhileLoop [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Body:");
        body.print(indent + 2);

        printIndent(indent + 1);
        System.out.println("Condition:");
        condition.print(indent + 2);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "DoWhileLoop [Line " + line + "]");
        String p = prefix + (isLast ? "    " : "│   ");

        tree(p, false, "Body");
        body.printTree(p + "│   ", true);

        tree(p, true, "Condition");
        condition.printTree(p + "    ", true);
    }
}

class ForNode extends ASTNode {
    private ASTNode init;
    private ASTNode condition;
    private ASTNode update;
    private ASTNode body;

    public ForNode(ASTNode init, ASTNode condition, ASTNode update, ASTNode body, int line, int column) {
        super(line, column);
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public ASTNode getInit() {
        return init;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getUpdate() {
        return update;
    }

    public ASTNode getBody() {
        return body;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("ForLoop [Line " + line + "]");

        if (init != null) {
            printIndent(indent + 1);
            System.out.println("Initialization:");
            init.print(indent + 2);
        }

        if (condition != null) {
            printIndent(indent + 1);
            System.out.println("Condition:");
            condition.print(indent + 2);
        }

        if (update != null) {
            printIndent(indent + 1);
            System.out.println("Update:");
            update.print(indent + 2);
        }

        printIndent(indent + 1);
        System.out.println("Body:");
        body.print(indent + 2);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "ForLoop [Line " + line + "]");
        String p = prefix + (isLast ? "    " : "│   ");

        if (init != null) init.printTree(p, false);
        if (condition != null) condition.printTree(p, false);
        if (update != null) update.printTree(p, false);

        body.printTree(p, true);
    }
}

class SwitchNode extends ASTNode {
    private ASTNode expression;
    private List<ASTNode> cases;
    private ASTNode defaultCase;

    public SwitchNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
        this.cases = new ArrayList<>();
    }

    public void addCase(ASTNode caseNode) {
        cases.add(caseNode);
    }

    public void setDefaultCase(ASTNode defaultCase) {
        this.defaultCase = defaultCase;
    }

    public ASTNode getExpression() {
        return expression;
    }

    public List<ASTNode> getCases() {
        return cases;
    }

    public ASTNode getDefaultCase() {
        return defaultCase;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("SwitchStatement [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Expression:");
        expression.print(indent + 2);

        for (ASTNode caseNode : cases) {
            caseNode.print(indent + 1);
        }

        if (defaultCase != null) {
            defaultCase.print(indent + 1);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "SwitchStatement [Line " + line + "]");
        String p = prefix + (isLast ? "    " : "│   ");

        expression.printTree(p, false);
        for (ASTNode c : cases) c.printTree(p, false);
        if (defaultCase != null) defaultCase.printTree(p, true);
    }
}

class CaseNode extends ASTNode {
    private ASTNode value;
    private List<ASTNode> statements;

    public CaseNode(ASTNode value, int line, int column) {
        super(line, column);
        this.value = value;
        this.statements = new ArrayList<>();
    }

    public void addStatement(ASTNode statement) {
        statements.add(statement);
    }

    public ASTNode getValue() {
        return value;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Case [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Value:");
        value.print(indent + 2);

        if (!statements.isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Statements:");
            for (ASTNode stmt : statements) {
                stmt.print(indent + 2);
            }
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Case [Line " + line + "]");
        value.printTree(prefix + "    ", false);
        for (ASTNode s : statements) s.printTree(prefix + "    ", true);
    }
}

class DefaultCaseNode extends ASTNode {
    private List<ASTNode> statements;

    public DefaultCaseNode(int line, int column) {
        super(line, column);
        this.statements = new ArrayList<>();
    }

    public void addStatement(ASTNode statement) {
        statements.add(statement);
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Default [Line " + line + "]");

        if (!statements.isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Statements:");
            for (ASTNode stmt : statements) {
                stmt.print(indent + 2);
            }
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Default [Line " + line + "]");
        for (ASTNode s : statements) s.printTree(prefix + "    ", true);
    }
}

class ReturnNode extends ASTNode {
    private ASTNode expression;

    public ReturnNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    public ASTNode getExpression() {
        return expression;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("ReturnStatement [Line " + line + "]");
        if (expression != null) {
            printIndent(indent + 1);
            System.out.println("Expression:");
            expression.print(indent + 2);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "ReturnStatement [Line " + line + "]");
        if (expression != null)
            expression.printTree(prefix + "    ", true);
    }
}

class BreakNode extends ASTNode {

    public BreakNode(int line, int column) {
        super(line, column);
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("BreakStatement [Line " + line + "]");
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "BreakStatement [Line " + line + "]");
    }
}

class ContinueNode extends ASTNode {

    public ContinueNode(int line, int column) {
        super(line, column);
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("ContinueStatement [Line " + line + "]");
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "ContinueStatement [Line " + line + "]");
    }
}

class BlockNode extends ASTNode {
    private List<ASTNode> statements;

    public BlockNode(List<ASTNode> statements, int line, int column) {
        super(line, column);
        this.statements = statements;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Block [Line " + line + "]");
        for (ASTNode stmt : statements) {
            stmt.print(indent + 1);
        }
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Block [Line " + line);
        String p = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < statements.size(); i++) {
            statements.get(i).printTree(p, i == statements.size() - 1);
        }
    }
}

class BinaryOpNode extends ASTNode {
    private String operator;
    private ASTNode left;
    private ASTNode right;

    public BinaryOpNode(String operator, ASTNode left, ASTNode right, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("BinaryOp: " + operator + " [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Left:");
        left.print(indent + 2);

        printIndent(indent + 1);
        System.out.println("Right:");
        right.print(indent + 2);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "BinaryOp: " + operator);
        left.printTree(prefix + "    ", false);
        right.printTree(prefix + "    ", true);
    }
}

class UnaryOpNode extends ASTNode {
    private String operator;
    private ASTNode operand;
    private boolean isPrefix;

    public UnaryOpNode(String operator, ASTNode operand,boolean isPrefix, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.operand = operand;
        this.isPrefix = isPrefix;
    }

    public String getOperator() {
        return operator;
    }

    public ASTNode getOperand() {
        return operand;
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        String type = isPrefix ? "Prefix" : "Postfix";
        System.out.println("UnaryOp (" + type + "): " + operator + " [Line " + line + "]");

        printIndent(indent + 1);
        System.out.println("Operand:");
        operand.print(indent + 2);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "UnaryOp: " + operator+  "[Line " + line + "]");
        operand.printTree(prefix + "    ", true);
    }
}

class LiteralNode extends ASTNode {
    private String type;
    private String value;

    public LiteralNode(String type, String value, int line, int column) {
        super(line, column);
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Literal: " + type + " = " + value);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Literal: " +type+ " = "+ value);
    }
}

class IdentifierNode extends ASTNode {
    private String name;

    public IdentifierNode(String name, int line, int column) {
        super(line, column);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("Identifier: " + name);
    }

    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "Identifier: " + name);
    }
}

class MethodCallNode extends ASTNode {
    private String methodName;
    private List<ASTNode> arguments;

    public MethodCallNode(String methodName, int line, int column) {
        super(line, column);
        this.methodName = methodName;
        this.arguments = new ArrayList<>();
    }

    public void setArguments(List<ASTNode> arguments) {
        this.arguments = arguments;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ASTNode> getArguments() {
        return arguments;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("MethodCall: " + methodName + " [Line " + line + "]");

        if (!arguments.isEmpty()) {
            printIndent(indent + 1);
            System.out.println("Arguments:");
            for (ASTNode arg : arguments) {
                arg.print(indent + 2);
            }
        }
    }
    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "MethodCall: " + methodName+ " [Line " + line + "]");
        for (ASTNode a : arguments)
            a.printTree(prefix + "    ", true);
    }}

class MemberAccessNode extends ASTNode {
    private List<String> members;
    private ASTNode methodCall;

    public MemberAccessNode(int line, int column) {
        super(line, column);
        this.members = new ArrayList<>();
    }

    public void addMember(String member) {
        members.add(member);
    }
    public List<String> getMembers(){
        return members;
    }

    public ASTNode getMethodCall(){
        return methodCall;
    }

    public void setMethodCall(ASTNode methodCall) {
        this.methodCall = methodCall;
    }

    @Override
    public void print(int indent) {
        printIndent(indent);
        System.out.println("MemberAccess: " + String.join(".", members) + " [Line " + line + "]");

        if (methodCall != null) {
            methodCall.print(indent + 1);
        }
    }
    @Override
    public void printTree(String prefix, boolean isLast) {
        tree(prefix, isLast, "MemberAccess: " + String.join(".", members)+ " [Line " + line + "]");
        if (methodCall != null) {
            methodCall.printTree(prefix,isLast);
        }
    }
}