import parser.ASTNode;

import java.io.*;

/**
 * Main driver for JavaParser (Phases 1 & 2 only - No AST)
 * Usage: java Main <input_file.java>
 */
public class Main {
    public static void main(String[] args) {
        // Check if filename argument is provided

        if (args.length < 1) {
            System.err.println("Usage: java Main <input_file.java>");
            System.exit(1);
        }

        String filename = args[0];

        try {
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║   Java Compiler - Full Implementation ║");
            System.out.println("║   Phase 1: Lexical Analysis           ║");
            System.out.println("║   Phase 2: Syntax Analysis             ║");
            System.out.println("║   Phase 3: AST Construction            ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Input file: " + filename);
            System.out.println();

            // Create parser with input file
            FileInputStream fis = new FileInputStream(filename);
            JavaParser parser = new JavaParser(fis);

            System.out.println("========================================");
            System.out.println("PHASE 2: PARSING PROGRAM STRUCTURE");
            System.out.println("========================================");

            // Parse the input and build AST (Phase 2 & 3)
            ASTNode ast = parser.Program();

            // If we reach here, parsing was successful
            System.out.println();
            System.out.println("SYNTAX VALIDATION SUCCESSFUL!");
            System.out.println();

            // Display parsing statistics
            parser.printStatistics();
            System.out.println();

            // Display symbol table (Phase 1)
            System.out.println("========================================");
            System.out.println("PHASE 1: SYMBOL TABLE (All Tokens)");
            System.out.println("========================================");
            parser.printSymbolTable();
            System.out.println();

            // Display variable information
            System.out.println("========================================");
            System.out.println("VARIABLE INFORMATION");
            System.out.println("========================================");
            parser.printVariables();
            System.out.println();
            // Display AST (Phase 3)
            System.out.println("========================================");
            System.out.println("PHASE 3: ABSTRACT SYNTAX TREE (AST)");
            System.out.println("========================================");
            ast.print(0);
            ast.printTree("",true);
            System.out.println();
            // Check for unused variables
            java.util.List<String> unused = parser.getUnusedVariables();
            if (!unused.isEmpty()) {
                System.out.println("========================================");
                System.out.println("WARNINGS: UNUSED VARIABLES");
                System.out.println("========================================");
                for (String var : unused) {
                    System.out.println(" -> " + var);
                }
                System.out.println();
            }

            // Close the input stream
            fis.close();

            // Summary
            System.out.println("========================================");
            System.out.println("COMPILATION SUMMARY");
            System.out.println("========================================");
            System.out.println("Phase 1 (Lexical Analysis): Complete");
            System.out.println("Phase 2 (Syntax Analysis): Complete");
            System.out.println("No syntax errors detected");
            System.out.println();

        } catch (FileNotFoundException e) {
            System.err.println("✗ ERROR: File '" + filename + "' not found.");
            System.exit(1);

        } catch (ParseException e) {
            // Handle syntax errors
            System.err.println();
            System.err.println("╔════════════════════════════════════════╗");
            System.err.println("║    SYNTAX ERROR DETECTED               ║");
            System.err.println("╚════════════════════════════════════════╝");
            System.err.println();
            System.err.println("Location: Line " + e.currentToken.next.beginLine +
                    ", Column " + e.currentToken.next.beginColumn);
            System.err.println();

            // Display error message
            System.err.println("Error Message:");
            System.err.println("  " + e.getMessage());
            System.err.println();

            // Try to provide more helpful context
            if (e.currentToken != null && e.currentToken.next != null) {
                System.err.println("Unexpected token: \"" + e.currentToken.next.image + "\"");
                System.err.println();
            }

            System.exit(1);

        } catch (TokenMgrError e) {
            // Handle lexical errors (invalid tokens)
            System.err.println();
            System.err.println("╔════════════════════════════════════════╗");
            System.err.println("║   ✗ LEXICAL ERROR DETECTED             ║");
            System.err.println("╚════════════════════════════════════════╝");
            System.err.println();
            System.err.println("Invalid token or character encountered");
            System.err.println();
            System.err.println("Error Message:");
            System.err.println("  " + e.getMessage());
            System.err.println();
            System.err.println("Common causes:");
            System.err.println("  • Invalid character in source code");
            System.err.println("  • Unclosed string literal");
            System.err.println("  • Unclosed comment");
            System.err.println("  • Invalid number format");
            System.err.println();

            System.exit(1);

        } catch (IOException e) {
            System.err.println("✗ ERROR: Could not read file: " + e.getMessage());
            System.exit(1);

        }catch ( RuntimeException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }catch (Exception e) {
            System.err.println("✗ UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


}