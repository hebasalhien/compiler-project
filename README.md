## How to Run the Project

You can execute the full workflow in sequence:

```bash
# Navigate to parser directory
cd src/parser 
# Generate parser using JavaCC
javacc JavaParser.jj 
# Compile source files
javac *.java
# Run the program with input file
java parser.JavaParser D:\java\compiler\input\valid.txt
