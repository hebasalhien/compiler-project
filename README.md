## How to Run the Project

You can execute the full workflow in sequence:

```bash
# Navigate to parser directory
cd src/parser 
# Generate parser using JavaCC
javacc JavaParser.jj 
# Return to project root
cd ../.. 
# Compile source files
javac src/parser/*.java src/Main.java 
# Run the program with input file
java -cp src Main input/valid.txt
