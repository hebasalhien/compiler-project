two classes in same file

boolean x = "";  // show warning in code

```java
String x = "ahmed";
String m = "kamel";

x - m;
x * m;
x / m;
```

```java
// problem 2 : variables not tracked
void func(){
    int x = 5;
}

x = 3; 

```

```java
// Problem 3: No redeclaration check
String x = "ahmed";

String x = "mohamed" ❌
```

### JavaParser

#### change #12

MethodDeclaration(), Bloc()

- Parameters live in method scope
- Local variables live in method scope
- Prevents leaking variables outside method

#### change #13

- add `Bloc` to
    `if, while, do while`
    instead of `Statement`
