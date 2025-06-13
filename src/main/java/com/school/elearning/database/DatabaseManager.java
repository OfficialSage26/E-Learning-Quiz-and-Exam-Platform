package com.school.elearning.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:elearning_quiz.db"; // Creates DB file in project root

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Drop relevant tables in order of dependency to ensure schema is always fresh for development
            System.out.println("Dropping quiz-related tables for schema refresh...");
            stmt.execute("DROP TABLE IF EXISTS student_answers;");
            stmt.execute("DROP TABLE IF EXISTS answer_options;");
            stmt.execute("DROP TABLE IF EXISTS quiz_attempts;"); // Depends on users and quizzes
            stmt.execute("DROP TABLE IF EXISTS questions;");   // Depends on quizzes
            stmt.execute("DROP TABLE IF EXISTS quizzes;");     // The table with the schema change
            // Users table is not dropped to preserve user accounts.
            System.out.println("Quiz-related tables dropped.");

            // Create Users table
            String createUserTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                      "username TEXT NOT NULL UNIQUE, " +
                                      // TODO: Store hashed passwords, not plain text
                                      "password TEXT NOT NULL, " +
                                      "registration_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                      ");";
            stmt.execute(createUserTableSql);
            System.out.println("Users table checked/created successfully.");

            // Create Quizzes table
            String createQuizzesTableSql = "CREATE TABLE IF NOT EXISTS quizzes (" +
                                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                         "title TEXT NOT NULL, " +
                                         "subject TEXT NOT NULL, " +
                                         "difficulty_level TEXT NOT NULL, " +
                                         "time_limit_minutes INTEGER NOT NULL DEFAULT 30" +
                                         // FOREIGN KEY(subject_id) REFERENCES subjects(id) -- For future use
                                         ");";
            stmt.execute(createQuizzesTableSql);
            System.out.println("Quizzes table checked/created successfully.");

            // Create Questions table
            String createQuestionsTableSql = "CREATE TABLE IF NOT EXISTS questions (" +
                                           "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                           "quiz_id INTEGER NOT NULL, " +
                                           "question_text TEXT NOT NULL, " +
                                           "question_type TEXT NOT NULL, " +
                                           "image_path TEXT, " +
                                           "FOREIGN KEY(quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                                           ");";
            stmt.execute(createQuestionsTableSql);
            System.out.println("Questions table checked/created successfully.");

            // Create AnswerOptions table
            String createAnswerOptionsTableSql = "CREATE TABLE IF NOT EXISTS answer_options (" +
                                               "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                               "question_id INTEGER NOT NULL, " +
                                               "option_text TEXT NOT NULL, " +
                                               "is_correct BOOLEAN NOT NULL DEFAULT 0, " +
                                               "FOREIGN KEY(question_id) REFERENCES questions(id) ON DELETE CASCADE" +
                                               ");";
            stmt.execute(createAnswerOptionsTableSql);
            System.out.println("AnswerOptions table checked/created successfully.");

            // Create QuizAttempts table
            String createQuizAttemptsTableSql = "CREATE TABLE IF NOT EXISTS quiz_attempts (" +
                                              "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                              "user_id INTEGER NOT NULL, " +
                                              "quiz_id INTEGER NOT NULL, " +
                                              "score REAL NOT NULL DEFAULT 0.0, " +
                                              "time_taken_seconds INTEGER NOT NULL DEFAULT 0, " +
                                              "attempt_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                              "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                                              "FOREIGN KEY(quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE" +
                                              ");";
            stmt.execute(createQuizAttemptsTableSql);
            System.out.println("QuizAttempts table checked/created successfully.");

            // Create StudentAnswers table
            String createStudentAnswersTableSql = "CREATE TABLE IF NOT EXISTS student_answers (" +
                                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                "quiz_attempt_id INTEGER NOT NULL, " +
                                                "question_id INTEGER NOT NULL, " +
                                                "selected_answer_option_id INTEGER, " +
                                                "short_answer_text TEXT, " +
                                                "is_correct BOOLEAN NOT NULL DEFAULT 0, " +
                                                "FOREIGN KEY(quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE, " +
                                                "FOREIGN KEY(question_id) REFERENCES questions(id) ON DELETE CASCADE, " +
                                                "FOREIGN KEY(selected_answer_option_id) REFERENCES answer_options(id) ON DELETE SET NULL" +
                                                ");";
            stmt.execute(createStudentAnswersTableSql);
            System.out.println("StudentAnswers table checked/created successfully.");

            // Populate sample data if tables are newly created or empty
            populateSampleData();

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Optional: Method to close resources, though try-with-resources is often sufficient
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
    
    // It's good practice to call initializeDatabase() once when the application starts.
    // This can be done in the MainApp class.

    public static void populateSampleData() {
        // String checkSql = "SELECT COUNT(*) FROM quizzes"; // No longer needed
        // String clearExistingQuestionsSql = "DELETE FROM questions;"; // No longer needed
        // String clearExistingAnswerOptionsSql = "DELETE FROM answer_options;"; // No longer needed
        // String clearExistingQuizzesSql = "DELETE FROM quizzes;"; // No longer needed

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
             // ResultSet rs = stmt.executeQuery(checkSql)) { // No longer needed

            // If quizzes exist, we assume we want to clear and repopulate with new structure.
            // This is a change from only populating if empty.
            // We will clear quizzes, questions, and answer_options to avoid orphaned data.
            // System.out.println("Clearing existing sample quiz data (if any) to populate new structure...");
            // stmt.execute(clearExistingAnswerOptionsSql); // No longer needed
            // stmt.execute(clearExistingQuestionsSql); // No longer needed
            // stmt.execute(clearExistingQuizzesSql); // No longer needed
            // System.out.println("Existing sample quiz data cleared."); // No longer needed

            System.out.println("Populating new sample quiz data with subjects...");

            // Subject: Discrete Mathematics (UGRD-CS6105)
            addQuizWithQuestions(conn, "Discrete Math - Easy", "UGRD-CS6105 Discrete Mathematics", "EASY", 10,
                new Object[][]{
                    {"What is a set in mathematics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A collection of distinct objects", true}, {"A sequence of numbers", false}, {"A type of graph", false}, {"A geometric shape", false}}},
                    {"Is a graph with no edges considered a valid graph?", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"The union of two sets A and B contains all elements that are in A, or in B, or in both.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What is the power set of {a, b}?", "MULTIPLE_CHOICE", null, new Object[][]{{"{ {}, {a}, {b}, {a,b} }", true}, {"{ {a}, {b} }", false}, {"{a, b}", false}, {"{ {}, {a,b} }", false}}},
                    {"A proposition that is always true is called a...", "SHORT_ANSWER", null, new Object[][]{{"Tautology", true}}},
                    {"A simple graph has no loops or multiple edges.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What does |A| represent if A is a set?", "SHORT_ANSWER", null, new Object[][]{{"The cardinality of A", true}}},
                    {"A sequence is a function from a set of integers to a set S.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"The Cartesian product of {1, 2} and {a, b} is...", "MULTIPLE_CHOICE", null, new Object[][]{{"{(1,a), (1,b), (2,a), (2,b)}", true}, {"{(1,a), (2,b)}", false}, {"{(1,2), (a,b)}", false}, {"{1,2,a,b}", false}}},
                    {"If a function is one-to-one and onto, it is called a bijection.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}}
                });
            addQuizWithQuestions(conn, "Discrete Math - Medium", "UGRD-CS6105 Discrete Mathematics", "MEDIUM", 20,
                new Object[][]{
                    {"What is the Big O notation for an algorithm with time complexity T(n) = 3n^2 + 5n + 2?", "SHORT_ANSWER", null,
                        new Object[][]{{"O(n^2)", true}}},
                    {"A function f: A -> B is injective if...", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Every element in B is mapped to by at least one element in A", false}, {"No two distinct elements in A map to the same element in B", true}, {"Every element in A maps to exactly one element in B", false}, {"It is both surjective and bijective", false}}},
                    {"What is a Hamiltonian circuit?", "SHORT_ANSWER", null, 
                        new Object[][]{{"A path in a graph that visits every vertex exactly once and returns to the starting vertex", true}}},
                    {"What is the time complexity of binary search?", "SHORT_ANSWER", null,
                        new Object[][]{{"O(log n)", true}}},
                    {"A complete graph with n vertices has how many edges?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"n(n-1)/2", true}, {"n(n+1)/2", false}, {"n^2", false}, {"n", false}}},
                    {"What is the principle of mathematical induction?", "SHORT_ANSWER", null,
                        new Object[][]{{"A method of mathematical proof that establishes a statement for all natural numbers", true}}},
                    {"What is a recurrence relation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"An equation that recursively defines a sequence", true}, {"A relation between two sets", false}, {"A type of graph", false}, {"A logical operator", false}}},
                    {"What is the difference between a permutation and a combination?", "SHORT_ANSWER", null,
                        new Object[][]{{"Permutation considers order, combination does not", true}}},
                    {"What is the chromatic number of a complete graph with n vertices?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"n", true}, {"n-1", false}, {"n+1", false}, {"n/2", false}}},
                    {"What is the principle of inclusion-exclusion?", "SHORT_ANSWER", null,
                        new Object[][]{{"A counting technique that accounts for overlapping sets", true}}},
                    {"What is a spanning tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A subgraph that is a tree and connects all vertices", true}, {"A tree with maximum height", false}, {"A tree with minimum height", false}, {"A tree with equal number of leaves", false}}},
                    {"What is the difference between a relation and a function?", "SHORT_ANSWER", null,
                        new Object[][]{{"A function is a relation where each input has exactly one output", true}}},
                    {"What is the time complexity of merge sort?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(n log n)", true}, {"O(n^2)", false}, {"O(n)", false}, {"O(log n)", false}}},
                    {"What is a bipartite graph?", "SHORT_ANSWER", null,
                        new Object[][]{{"A graph whose vertices can be divided into two disjoint sets", true}}},
                    {"What is the principle of strong induction?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A form of induction that assumes the statement is true for all values less than n", true}, {"A form of induction that only works for even numbers", false}, {"A form of induction that only works for prime numbers", false}, {"A form of induction that only works for perfect squares", false}}},
                    {"What is the difference between a partial order and a total order?", "SHORT_ANSWER", null,
                        new Object[][]{{"In a total order, every pair of elements is comparable", true}}},
                    {"What is the time complexity of matrix multiplication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(n^3)", true}, {"O(n^2)", false}, {"O(n)", false}, {"O(log n)", false}}},
                    {"What is a recurrence relation for the Fibonacci sequence?", "SHORT_ANSWER", null,
                        new Object[][]{{"F(n) = F(n-1) + F(n-2)", true}}},
                    {"What is the difference between a directed and undirected graph?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"In a directed graph, edges have direction", true}, {"In a directed graph, vertices have direction", false}, {"In a directed graph, edges have weight", false}, {"In a directed graph, vertices have weight", false}}},
                    {"What is the principle of the pigeonhole principle?", "SHORT_ANSWER", null,
                        new Object[][]{{"If n items are put into m containers with n > m, at least one container must contain more than one item", true}}},
                    {"What is the time complexity of quicksort in the worst case?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(n^2)", true}, {"O(n log n)", false}, {"O(n)", false}, {"O(log n)", false}}},
                    {"What is a Eulerian circuit?", "SHORT_ANSWER", null,
                        new Object[][]{{"A path that visits every edge exactly once and returns to the starting vertex", true}}},
                    {"What is the difference between a surjective and injective function?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A surjective function covers all elements in the codomain, while an injective function maps distinct elements to distinct elements", true}, {"A surjective function is one-to-one, while an injective function is onto", false}, {"A surjective function is bijective, while an injective function is not", false}, {"A surjective function is reflexive, while an injective function is not", false}}},
                    {"What is the principle of the well-ordering principle?", "SHORT_ANSWER", null,
                        new Object[][]{{"Every non-empty set of positive integers has a least element", true}}},
                    {"What is the time complexity of binary tree traversal?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(n)", true}, {"O(log n)", false}, {"O(n^2)", false}, {"O(1)", false}}},
                    {"What is a recurrence relation for the Tower of Hanoi problem?", "SHORT_ANSWER", null,
                        new Object[][]{{"T(n) = 2T(n-1) + 1", true}}},
                    {"What is the difference between a tree and a forest?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A forest is a collection of trees", true}, {"A forest has more edges than a tree", false}, {"A forest has more vertices than a tree", false}, {"A forest is a type of tree", false}}},
                    {"What is the principle of the principle of mathematical induction?", "SHORT_ANSWER", null,
                        new Object[][]{{"If a statement is true for n=1 and if it's true for n=k implies it's true for n=k+1, then it's true for all positive integers", true}}},
                    {"What is the time complexity of finding the shortest path in a graph using Dijkstra's algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O((V+E)log V)", true}, {"O(V^2)", false}, {"O(E)", false}, {"O(V)", false}}},
                    {"What is a recurrence relation for the merge sort algorithm?", "SHORT_ANSWER", null,
                        new Object[][]{{"T(n) = 2T(n/2) + n", true}}}
                });
            addQuizWithQuestions(conn, "Discrete Math - Hard", "UGRD-CS6105 Discrete Mathematics", "HARD", 30,
                new Object[][]{
                    {"Prove by induction that the sum of the first n odd positive integers is n^2.", "SHORT_ANSWER", null,
                        new Object[][]{{"Base case P(1): 1 = 1^2. Assume P(k) is true: 1+3+...+(2k-1)=k^2. Then 1+3+...+(2k-1)+(2k+1) = k^2 + 2k + 1 = (k+1)^2. Thus P(k+1) is true.", true}}},
                    {"What is the pigeonhole principle?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"If n items are put into m containers, with n > m, then at least one container must contain more than one item.", true}, {"Every planar graph can be colored with 4 colors.", false}, {"The shortest path between two nodes in a graph.", false}}},
                    {"Prove that a graph is bipartite if and only if it contains no odd cycles.", "SHORT_ANSWER", null,
                        new Object[][]{{"A bipartite graph can be colored with 2 colors. If it has an odd cycle, it would require 3 colors, making it impossible to be bipartite. Conversely, if it has no odd cycles, it can be colored with 2 colors.", true}}},
                    {"What is the time complexity of the Floyd-Warshall algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(V^3)", true}, {"O(V^2)", false}, {"O(E log V)", false}, {"O(V log V)", false}}},
                    {"Prove that the number of edges in a complete graph with n vertices is n(n-1)/2.", "SHORT_ANSWER", null,
                        new Object[][]{{"Each vertex connects to n-1 other vertices. Total edges = n(n-1)/2 because each edge is counted twice.", true}}},
                    {"What is the chromatic number of a planar graph?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"At most 4 (Four Color Theorem)", true}, {"At most 3", false}, {"At most 5", false}, {"At most 6", false}}},
                    {"Prove that every tree with n vertices has exactly n-1 edges.", "SHORT_ANSWER", null,
                        new Object[][]{{"By induction: Base case n=1 has 0 edges. Adding a vertex requires exactly one new edge to maintain connectivity without cycles.", true}}},
                    {"What is the time complexity of finding the maximum flow in a network?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(VE^2) for Ford-Fulkerson with BFS", true}, {"O(V^2E)", false}, {"O(E log V)", false}, {"O(V log E)", false}}},
                    {"Prove that a graph is Eulerian if and only if it is connected and every vertex has even degree.", "SHORT_ANSWER", null,
                        new Object[][]{{"An Eulerian circuit must enter and leave each vertex equally, requiring even degree. Connectedness ensures a single circuit.", true}}},
                    {"What is the time complexity of the Bellman-Ford algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(VE)", true}, {"O(V^2)", false}, {"O(E log V)", false}, {"O(V log E)", false}}},
                    {"Prove that a graph is Hamiltonian if it has n vertices and every vertex has degree at least n/2.", "SHORT_ANSWER", null,
                        new Object[][]{{"By Dirac's theorem: If G is a simple graph with n vertices and each vertex has degree at least n/2, then G is Hamiltonian.", true}}},
                    {"What is the time complexity of the Kruskal's algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(E log E)", true}, {"O(V^2)", false}, {"O(E log V)", false}, {"O(V log E)", false}}},
                    {"Prove that a tree is a minimally connected graph.", "SHORT_ANSWER", null,
                        new Object[][]{{"A tree is connected and removing any edge disconnects it, making it minimally connected.", true}}},
                    {"What is the time complexity of the Prim's algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(E log V)", true}, {"O(V^2)", false}, {"O(E log E)", false}, {"O(V log E)", false}}},
                    {"Prove that a graph is planar if and only if it does not contain K5 or K3,3 as a minor.", "SHORT_ANSWER", null,
                        new Object[][]{{"By Kuratowski's theorem: A graph is planar if and only if it does not contain K5 or K3,3 as a minor.", true}}},
                    {"What is the time complexity of the Dijkstra's algorithm with Fibonacci heap?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(V log V + E)", true}, {"O(V^2)", false}, {"O(E log V)", false}, {"O(V log E)", false}}},
                    {"Prove that a graph is 2-colorable if and only if it is bipartite.", "SHORT_ANSWER", null,
                        new Object[][]{{"A 2-coloring divides vertices into two sets with no edges within sets, which is the definition of bipartite.", true}}},
                    {"What is the time complexity of the Johnson's algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(V^2 log V + VE)", true}, {"O(V^3)", false}, {"O(VE log V)", false}, {"O(V^2 log E)", false}}},
                    {"Prove that a graph is a tree if and only if it is connected and has no cycles.", "SHORT_ANSWER", null,
                        new Object[][]{{"A tree is minimally connected (no cycles) and maximally acyclic (connected).", true}}},
                    {"What is the time complexity of the Ford-Fulkerson algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(E|f|) where |f| is the maximum flow", true}, {"O(VE)", false}, {"O(V^2E)", false}, {"O(E log V)", false}}},
                    {"Prove that a graph is Eulerian if and only if it is connected and every vertex has even degree.", "SHORT_ANSWER", null,
                        new Object[][]{{"An Eulerian circuit must enter and leave each vertex equally, requiring even degree. Connectedness ensures a single circuit.", true}}},
                    {"What is the time complexity of the Hungarian algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(V^3)", true}, {"O(V^2)", false}, {"O(VE)", false}, {"O(V log V)", false}}},
                    {"Prove that a graph is Hamiltonian if it has n vertices and the sum of degrees of any two non-adjacent vertices is at least n.", "SHORT_ANSWER", null,
                        new Object[][]{{"By Ore's theorem: If G is a simple graph with n vertices and for any two non-adjacent vertices, the sum of their degrees is at least n, then G is Hamiltonian.", true}}},
                    {"What is the time complexity of the Floyd-Warshall algorithm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"O(V^3)", true}, {"O(V^2)", false}, {"O(VE)", false}, {"O(V log V)", false}}},
                    {"Prove that a graph is planar if and only if it does not contain K5 or K3,3 as a subgraph.", "SHORT_ANSWER", null,
                        new Object[][]{{"By Kuratowski's theorem: A graph is planar if and only if it does not contain K5 or K3,3 as a subgraph.", true}}}
                });

            // Subject: Computer Programming 2 (UGRD-ITE6104)
            addQuizWithQuestions(conn, "Java Basics - Easy", "UGRD-ITE6104 Computer Programming 2", "EASY", 10,
                new Object[][]{
                    {"What keyword is used to define a class in Java?", "SHORT_ANSWER", null, new Object[][]{{"class", true}}},
                    {"Is Java case-sensitive?", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What does 'static' mean in a Java method declaration?", "MULTIPLE_CHOICE", null, new Object[][]{{"The method belongs to the class, not an instance", true}, {"The method cannot be changed", false}, {"The method is thread-safe", false}, {"The method can only be called once", false}}},
                    {"An 'int' is a reference type in Java.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}},
                    {"What is the 'main' method's signature in Java?", "SHORT_ANSWER", null, new Object[][]{{"public static void main(String[] args)", true}}},
                    {"Which keyword is used to inherit a class in Java?", "SHORT_ANSWER", null, new Object[][]{{"extends", true}}},
                    {"A constructor must have the same name as the class.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What is the purpose of the 'super' keyword?", "MULTIPLE_CHOICE", null, new Object[][]{{"To call the parent class's constructor or methods", true}, {"To create a superclass", false}, {"To define a final variable", false}, {"To mark a method as important", false}}},
                    {"Java supports multiple inheritance of classes.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}},
                    {"What is an object in Java?", "SHORT_ANSWER", null, new Object[][]{{"An instance of a class", true}}}
                });
            addQuizWithQuestions(conn, "OOP Concepts - Medium", "UGRD-ITE6104 Computer Programming 2", "MEDIUM", 20,
                new Object[][]{
                    {"What is polymorphism in OOP?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"The ability of an object to take on many forms", true}, {"Hiding internal state and requiring all interaction to be performed through an object's methods", false}, {"The process of defining a new class from an existing class", false}}},
                    {"Explain encapsulation.", "SHORT_ANSWER", null, 
                        new Object[][]{{"Bundling data and methods that operate on that data within a single unit or class", true}}},
                    {"What is method overriding?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Providing a specific implementation of a method that is already defined in a parent class", true}, {"Creating a new method with the same name in a different class", false}, {"Changing the method signature in a subclass", false}, {"Deleting a method from a parent class", false}}},
                    {"What is the difference between an interface and an abstract class?", "SHORT_ANSWER", null,
                        new Object[][]{{"An interface can only have abstract methods and constants, while an abstract class can have both abstract and concrete methods", true}}},
                    {"What is the purpose of the 'final' keyword in Java?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To prevent inheritance, overriding, or modification", true}, {"To make a method run faster", false}, {"To create a constant", false}, {"To mark a class as important", false}}},
                    {"What is a static method?", "SHORT_ANSWER", null,
                        new Object[][]{{"A method that belongs to the class rather than an instance of the class", true}}},
                    {"What is the difference between '==' and '.equals()' in Java?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"'==' compares references, while '.equals()' compares content", true}, {"'==' compares content, while '.equals()' compares references", false}, {"They are exactly the same", false}, {"'==' is for primitives, '.equals()' is for objects", false}}},
                    {"What is a constructor chain?", "SHORT_ANSWER", null,
                        new Object[][]{{"The sequence of constructor calls from a subclass to its parent class", true}}},
                    {"What is the purpose of the 'super' keyword?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To call the parent class's constructor or methods", true}, {"To create a superclass", false}, {"To define a final variable", false}, {"To mark a method as important", false}}},
                    {"What is method overloading?", "SHORT_ANSWER", null,
                        new Object[][]{{"Defining multiple methods with the same name but different parameters", true}}},
                    {"What is the difference between a shallow copy and a deep copy?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A shallow copy copies references, while a deep copy copies the actual objects", true}, {"A shallow copy is faster than a deep copy", false}, {"A deep copy is only for primitive types", false}, {"A shallow copy is only for objects", false}}},
                    {"What is the purpose of the 'this' keyword?", "SHORT_ANSWER", null,
                        new Object[][]{{"To refer to the current instance of the class", true}}},
                    {"What is a static block?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A block of code that runs when the class is loaded", true}, {"A block of code that runs when an object is created", false}, {"A block of code that runs when a method is called", false}, {"A block of code that runs when a variable is accessed", false}}},
                    {"What is the difference between 'public', 'private', 'protected', and 'default' access modifiers?", "SHORT_ANSWER", null,
                        new Object[][]{{"They control the visibility and accessibility of classes, methods, and variables", true}}},
                    {"What is a singleton pattern?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A design pattern that restricts the instantiation of a class to one object", true}, {"A pattern for creating multiple objects", false}, {"A pattern for inheritance", false}, {"A pattern for method overloading", false}}},
                    {"What is the purpose of the 'volatile' keyword?", "SHORT_ANSWER", null,
                        new Object[][]{{"To ensure that a variable's value is always read from and written to main memory", true}}},
                    {"What is the difference between 'String', 'StringBuilder', and 'StringBuffer'?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"String is immutable, while StringBuilder and StringBuffer are mutable, with StringBuffer being thread-safe", true}, {"String is mutable, while StringBuilder and StringBuffer are immutable", false}, {"They are all exactly the same", false}, {"String is thread-safe, while StringBuilder and StringBuffer are not", false}}},
                    {"What is a factory method pattern?", "SHORT_ANSWER", null,
                        new Object[][]{{"A creational pattern that provides an interface for creating objects but lets subclasses decide which class to instantiate", true}}},
                    {"What is the purpose of the 'transient' keyword?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To indicate that a field should not be serialized", true}, {"To make a variable temporary", false}, {"To make a method run faster", false}, {"To mark a class as important", false}}},
                    {"What is the difference between 'checked' and 'unchecked' exceptions?", "SHORT_ANSWER", null,
                        new Object[][]{{"Checked exceptions must be handled or declared, while unchecked exceptions don't", true}}},
                    {"What is a design pattern?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A reusable solution to common problems in software design", true}, {"A way to write code faster", false}, {"A type of algorithm", false}, {"A way to debug code", false}}},
                    {"What is the purpose of the 'synchronized' keyword?", "SHORT_ANSWER", null,
                        new Object[][]{{"To prevent multiple threads from executing a method or block simultaneously", true}}},
                    {"What is the difference between 'ArrayList' and 'LinkedList'?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"ArrayList is backed by an array, while LinkedList is backed by a doubly-linked list", true}, {"ArrayList is faster than LinkedList", false}, {"LinkedList is faster than ArrayList", false}, {"They are exactly the same", false}}},
                    {"What is a callback function?", "SHORT_ANSWER", null,
                        new Object[][]{{"A function that is passed as an argument to another function and is executed after the main function has finished", true}}},
                    {"What is the purpose of the 'assert' keyword?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To test assumptions about the program", true}, {"To handle exceptions", false}, {"To create a new object", false}, {"To mark a method as important", false}}},
                    {"What is the difference between 'HashMap' and 'HashTable'?", "SHORT_ANSWER", null,
                        new Object[][]{{"HashMap is not synchronized and allows null values, while HashTable is synchronized and doesn't allow null values", true}}},
                    {"What is a lambda expression?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"A short block of code that takes in parameters and returns a value", true}, {"A way to create objects", false}, {"A type of exception", false}, {"A way to debug code", false}}},
                    {"What is the purpose of the 'enum' keyword?", "SHORT_ANSWER", null,
                        new Object[][]{{"To define a special class that represents a group of constants", true}}},
                    {"What is the difference between 'Comparable' and 'Comparator'?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Comparable is implemented by the class itself, while Comparator is implemented in a separate class", true}, {"Comparable is faster than Comparator", false}, {"Comparator is faster than Comparable", false}, {"They are exactly the same", false}}},
                    {"What is a stream in Java?", "SHORT_ANSWER", null,
                        new Object[][]{{"A sequence of elements supporting sequential and parallel aggregate operations", true}}}
                });
             addQuizWithQuestions(conn, "Data Structures - Hard", "UGRD-ITE6102 Data Structures", "HARD", 30,
                new Object[][]{
                    {"Analyze the time complexity of the Hopcroft-Karp algorithm for maximum bipartite matching.", "SHORT_ANSWER", null,
                        new Object[][]{{"O(√V * E) where V is the number of vertices and E is the number of edges.", true}}},
                    {"Which data structure is most efficient for implementing a priority queue with O(1) insertion and O(log n) deletion?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Fibonacci Heap", true}, {"Binary Heap", false}, {"AVL Tree", false}, {"Red-Black Tree", false}}},
                    {"Explain the concept of amortized analysis in data structures.", "SHORT_ANSWER", null,
                        new Object[][]{{"Amortized analysis provides a way to analyze the average time complexity of operations over a sequence, rather than worst-case for individual operations.", true}}},
                    {"What is the difference between a B-tree and a B+ tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"B+ trees store data only in leaf nodes and have linked leaves, while B-trees store data in all nodes", true}, {"They are exactly the same", false}, {"B-trees are faster", false}, {"B+ trees are simpler", false}}},
                    {"Analyze the space-time tradeoff in implementing a suffix tree.", "SHORT_ANSWER", null,
                        new Object[][]{{"Suffix trees use O(n) space to store all suffixes of a string, enabling O(m) pattern matching where m is pattern length.", true}}},
                    {"What is the purpose of a skip list?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To provide O(log n) search time with simpler implementation than balanced trees", true}, {"To skip elements", false}, {"To make lists faster", false}, {"To reduce memory usage", false}}},
                    {"Explain the concept of persistent data structures.", "SHORT_ANSWER", null,
                        new Object[][]{{"Persistent data structures maintain previous versions when modified, allowing efficient access to historical states.", true}}},
                    {"What is the difference between a trie and a radix tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Radix trees compress single-child nodes, while tries store each character in a separate node", true}, {"They are exactly the same", false}, {"Tries are faster", false}, {"Radix trees are simpler", false}}},
                    {"Analyze the implementation of a concurrent hash map.", "SHORT_ANSWER", null,
                        new Object[][]{{"Concurrent hash maps use techniques like lock striping and atomic operations to allow concurrent access while maintaining consistency.", true}}},
                    {"What is the role of a bloom filter?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To efficiently test set membership with possible false positives but no false negatives", true}, {"To filter data", false}, {"To sort elements", false}, {"To compress data", false}}}
                });

            // Subject: Ethics (UGRD-GE6107)
            addQuizWithQuestions(conn, "Ethical Theories - Easy", "UGRD-GE6107 Ethics", "EASY", 10,
                new Object[][]{
                    {"Utilitarianism focuses on...", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Consequences", true}, {"Duties", false}, {"Virtues", false}, {"Rights", false}}},
                    {"Deontology is an ethical theory that emphasizes duties or rules.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"Virtue ethics is primarily concerned with the character of the moral agent.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"The 'Golden Rule' is an example of which ethical principle?", "MULTIPLE_CHOICE", null, new Object[][]{{"Reciprocity", true}, {"Utility", false}, {"Justice", false}, {"Rights", false}}},
                    {"What is 'moral relativism'?", "SHORT_ANSWER", null, new Object[][]{{"The view that moral judgments are true or false only relative to some particular standpoint", true}}},
                    {"Beneficence in ethics means...", "MULTIPLE_CHOICE", null, new Object[][]{{"To act for the benefit of others", true}, {"To do no harm", false}, {"To be fair", false}, {"To respect autonomy", false}}},
                    {"Informed consent is a key principle in research ethics.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What is a conflict of interest?", "SHORT_ANSWER", null, new Object[][]{{"A situation where personal interests could compromise professional judgment", true}}},
                    {"Ethical egoism is the theory that one's self is, or should be, the motivation and the goal of one's own action.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What does 'autonomy' mean in medical ethics?", "SHORT_ANSWER", null, new Object[][]{{"The right of patients to make decisions about their medical care", true}}}
                });
            addQuizWithQuestions(conn, "Applied Ethics - Medium", "UGRD-GE6107 Ethics", "MEDIUM", 20,
                new Object[][]{
                    {"What is a common ethical dilemma in AI development?", "SHORT_ANSWER", null,
                        new Object[][]{{"Bias in algorithms or job displacement", true}}},
                    {"Define 'informed consent' in medical ethics.", "SHORT_ANSWER", null, 
                        new Object[][]{{"A process for getting permission before conducting a healthcare intervention on a person", true}}},
                    {"What is the difference between deontological and consequentialist ethics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Deontological ethics focuses on rules and duties, while consequentialist ethics focuses on outcomes", true}, {"Deontological ethics is about intentions, while consequentialist ethics is about actions", false}, {"Deontological ethics is about virtue, while consequentialist ethics is about character", false}, {"Deontological ethics is about rights, while consequentialist ethics is about responsibilities", false}}},
                    {"What is the principle of double effect?", "SHORT_ANSWER", null,
                        new Object[][]{{"A principle that allows an action that causes harm as a side effect of promoting a good end", true}}},
                    {"What is the difference between ethical relativism and ethical absolutism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical relativism holds that moral values are relative to culture, while ethical absolutism holds that moral values are universal", true}, {"Ethical relativism is about individual choice, while ethical absolutism is about group consensus", false}, {"Ethical relativism is about consequences, while ethical absolutism is about intentions", false}, {"Ethical relativism is about rights, while ethical absolutism is about duties", false}}},
                    {"What is the concept of moral responsibility?", "SHORT_ANSWER", null,
                        new Object[][]{{"The idea that individuals are accountable for their actions and their consequences", true}}},
                    {"What is the difference between positive and negative rights?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Positive rights require action from others, while negative rights require others to refrain from action", true}, {"Positive rights are about freedom, while negative rights are about equality", false}, {"Positive rights are about individuals, while negative rights are about groups", false}, {"Positive rights are about justice, while negative rights are about fairness", false}}},
                    {"What is the principle of beneficence?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ethical principle of doing good and promoting the well-being of others", true}}},
                    {"What is the difference between ethical egoism and altruism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical egoism promotes self-interest, while altruism promotes the interests of others", true}, {"Ethical egoism is about rights, while altruism is about duties", false}, {"Ethical egoism is about consequences, while altruism is about intentions", false}, {"Ethical egoism is about virtue, while altruism is about character", false}}},
                    {"What is the concept of moral luck?", "SHORT_ANSWER", null,
                        new Object[][]{{"The idea that factors beyond an agent's control can affect their moral responsibility", true}}},
                    {"What is the difference between act utilitarianism and rule utilitarianism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Act utilitarianism evaluates individual actions, while rule utilitarianism evaluates rules of action", true}, {"Act utilitarianism is about intentions, while rule utilitarianism is about consequences", false}, {"Act utilitarianism is about rights, while rule utilitarianism is about duties", false}, {"Act utilitarianism is about virtue, while rule utilitarianism is about character", false}}},
                    {"What is the principle of non-maleficence?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ethical principle of not causing harm to others", true}}},
                    {"What is the difference between moral rights and legal rights?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral rights are based on ethical principles, while legal rights are based on laws", true}, {"Moral rights are about individuals, while legal rights are about groups", false}, {"Moral rights are about freedom, while legal rights are about equality", false}, {"Moral rights are about justice, while legal rights are about fairness", false}}},
                    {"What is the concept of moral agency?", "SHORT_ANSWER", null,
                        new Object[][]{{"The capacity of an individual to make moral judgments and take moral actions", true}}},
                    {"What is the difference between ethical pluralism and ethical monism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical pluralism recognizes multiple valid ethical theories, while ethical monism recognizes only one", true}, {"Ethical pluralism is about consequences, while ethical monism is about intentions", false}, {"Ethical pluralism is about rights, while ethical monism is about duties", false}, {"Ethical pluralism is about virtue, while ethical monism is about character", false}}},
                    {"What is the principle of justice in ethics?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ethical principle of treating people fairly and equitably", true}}},
                    {"What is the difference between moral absolutism and moral particularism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral absolutism holds that moral rules are universal, while moral particularism holds that moral judgments depend on context", true}, {"Moral absolutism is about intentions, while moral particularism is about consequences", false}, {"Moral absolutism is about rights, while moral particularism is about duties", false}, {"Moral absolutism is about virtue, while moral particularism is about character", false}}},
                    {"What is the concept of moral standing?", "SHORT_ANSWER", null,
                        new Object[][]{{"The status of being a proper subject of moral consideration", true}}},
                    {"What is the difference between ethical naturalism and ethical non-naturalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical naturalism holds that moral properties are natural properties, while ethical non-naturalism holds they are not", true}, {"Ethical naturalism is about consequences, while ethical non-naturalism is about intentions", false}, {"Ethical naturalism is about rights, while ethical non-naturalism is about duties", false}, {"Ethical naturalism is about virtue, while ethical non-naturalism is about character", false}}},
                    {"What is the principle of autonomy?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ethical principle of respecting an individual's right to make their own decisions", true}}},
                    {"What is the difference between moral realism and moral anti-realism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral realism holds that moral facts exist independently of human beliefs, while moral anti-realism denies this", true}, {"Moral realism is about intentions, while moral anti-realism is about consequences", false}, {"Moral realism is about rights, while moral anti-realism is about duties", false}, {"Moral realism is about virtue, while moral anti-realism is about character", false}}},
                    {"What is the concept of moral responsibility?", "SHORT_ANSWER", null,
                        new Object[][]{{"The idea that individuals are accountable for their actions and their consequences", true}}},
                    {"What is the difference between ethical subjectivism and ethical objectivism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical subjectivism holds that moral judgments are based on individual feelings, while ethical objectivism holds they are based on objective facts", true}, {"Ethical subjectivism is about consequences, while ethical objectivism is about intentions", false}, {"Ethical subjectivism is about rights, while ethical objectivism is about duties", false}, {"Ethical subjectivism is about virtue, while ethical objectivism is about character", false}}},
                    {"What is the principle of veracity?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ethical principle of truthfulness and honesty", true}}},
                    {"What is the difference between moral particularism and moral generalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral particularism holds that moral judgments depend on context, while moral generalism holds they follow general rules", true}, {"Moral particularism is about intentions, while moral generalism is about consequences", false}, {"Moral particularism is about rights, while moral generalism is about duties", false}, {"Moral particularism is about virtue, while moral generalism is about character", false}}},
                    {"What is the concept of moral worth?", "SHORT_ANSWER", null,
                        new Object[][]{{"The value of an action based on its moral character", true}}},
                    {"What is the difference between ethical intuitionism and ethical rationalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical intuitionism holds that moral knowledge comes from intuition, while ethical rationalism holds it comes from reason", true}, {"Ethical intuitionism is about consequences, while ethical rationalism is about intentions", false}, {"Ethical intuitionism is about rights, while ethical rationalism is about duties", false}, {"Ethical intuitionism is about virtue, while ethical rationalism is about character", false}}},
                    {"What is the principle of fidelity?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ethical principle of keeping promises and being loyal", true}}},
                    {"What is the difference between moral skepticism and moral dogmatism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral skepticism doubts the possibility of moral knowledge, while moral dogmatism asserts it with certainty", true}, {"Moral skepticism is about intentions, while moral dogmatism is about consequences", false}, {"Moral skepticism is about rights, while moral dogmatism is about duties", false}, {"Moral skepticism is about virtue, while moral dogmatism is about character", false}}},
                    {"What is the concept of moral character?", "SHORT_ANSWER", null,
                        new Object[][]{{"The set of traits that make up a person's moral personality", true}}}
                });
            addQuizWithQuestions(conn, "Philosophical Ethics - Hard", "UGRD-GE6107 Ethics", "HARD", 30,
                new Object[][]{
                    {"Compare and contrast Virtue Ethics with Consequentialism.", "SHORT_ANSWER", null,
                        new Object[][]{{"Virtue ethics focuses on character traits; consequentialism focuses on outcomes of actions.", true}}},
                    {"What is Kant's Categorical Imperative? Explain one formulation.", "SHORT_ANSWER", null,
                        new Object[][]{{"Act only according to that maxim whereby you can at the same time will that it should become a universal law.", true}}},
                    {"Analyze the ethical implications of artificial intelligence in decision-making.", "SHORT_ANSWER", null,
                        new Object[][]{{"AI raises concerns about accountability, bias, transparency, and the delegation of moral decisions to non-human agents.", true}}},
                    {"What is the difference between moral absolutism and moral relativism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral absolutism holds that moral principles are universal, while moral relativism holds they are context-dependent", true}, {"Moral absolutism is about intentions, while moral relativism is about consequences", false}, {"Moral absolutism is religious, while moral relativism is secular", false}, {"Moral absolutism is modern, while moral relativism is traditional", false}}},
                    {"Explain the concept of moral luck and its implications for moral responsibility.", "SHORT_ANSWER", null,
                        new Object[][]{{"Moral luck occurs when factors beyond an agent's control affect their moral status. It challenges traditional notions of moral responsibility.", true}}},
                    {"What is the difference between act utilitarianism and rule utilitarianism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Act utilitarianism evaluates individual actions, while rule utilitarianism evaluates rules of action", true}, {"Act utilitarianism is about intentions, while rule utilitarianism is about consequences", false}, {"Act utilitarianism is about rights, while rule utilitarianism is about duties", false}, {"Act utilitarianism is about virtue, while rule utilitarianism is about character", false}}},
                    {"Analyze the ethical implications of genetic engineering in humans.", "SHORT_ANSWER", null,
                        new Object[][]{{"Genetic engineering raises concerns about playing God, eugenics, social inequality, and the definition of human nature.", true}}},
                    {"What is the difference between ethical egoism and altruism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical egoism promotes self-interest, while altruism promotes the interests of others", true}, {"Ethical egoism is about rights, while altruism is about duties", false}, {"Ethical egoism is about consequences, while altruism is about intentions", false}, {"Ethical egoism is about virtue, while altruism is about character", false}}},
                    {"Explain the concept of moral standing and its application to non-human entities.", "SHORT_ANSWER", null,
                        new Object[][]{{"Moral standing determines which entities deserve moral consideration. It's applied to animals, ecosystems, and AI, raising questions about the basis of moral value.", true}}},
                    {"What is the difference between positive and negative rights?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Positive rights require action from others, while negative rights require others to refrain from action", true}, {"Positive rights are about freedom, while negative rights are about equality", false}, {"Positive rights are about individuals, while negative rights are about groups", false}, {"Positive rights are about justice, while negative rights are about fairness", false}}},
                    {"Analyze the ethical implications of surveillance technology in society.", "SHORT_ANSWER", null,
                        new Object[][]{{"Surveillance technology raises concerns about privacy, autonomy, social control, and the balance between security and freedom.", true}}},
                    {"What is the difference between moral realism and moral anti-realism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral realism holds that moral facts exist independently of human beliefs, while moral anti-realism denies this", true}, {"Moral realism is about intentions, while moral anti-realism is about consequences", false}, {"Moral realism is about rights, while moral anti-realism is about duties", false}, {"Moral realism is about virtue, while moral anti-realism is about character", false}}},
                    {"Explain the concept of moral responsibility in the context of corporate ethics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Corporate moral responsibility involves questions about collective agency, individual vs. organizational responsibility, and the role of corporations in society.", true}}},
                    {"What is the difference between ethical naturalism and ethical non-naturalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical naturalism holds that moral properties are natural properties, while ethical non-naturalism holds they are not", true}, {"Ethical naturalism is about consequences, while ethical non-naturalism is about intentions", false}, {"Ethical naturalism is about rights, while ethical non-naturalism is about duties", false}, {"Ethical naturalism is about virtue, while ethical non-naturalism is about character", false}}},
                    {"Analyze the ethical implications of climate change and intergenerational justice.", "SHORT_ANSWER", null,
                        new Object[][]{{"Climate change raises questions about responsibility to future generations, distributive justice, and the ethical basis of environmental obligations.", true}}},
                    {"What is the difference between moral particularism and moral generalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral particularism holds that moral judgments depend on context, while moral generalism holds they follow general rules", true}, {"Moral particularism is about intentions, while moral generalism is about consequences", false}, {"Moral particularism is about rights, while moral generalism is about duties", false}, {"Moral particularism is about virtue, while moral generalism is about character", false}}},
                    {"Explain the concept of moral agency in the context of artificial intelligence.", "SHORT_ANSWER", null,
                        new Object[][]{{"Moral agency in AI raises questions about consciousness, autonomy, and whether machines can be moral agents or patients.", true}}},
                    {"What is the difference between ethical pluralism and ethical monism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical pluralism recognizes multiple valid ethical theories, while ethical monism recognizes only one", true}, {"Ethical pluralism is about consequences, while ethical monism is about intentions", false}, {"Ethical pluralism is about rights, while ethical monism is about duties", false}, {"Ethical pluralism is about virtue, while ethical monism is about character", false}}},
                    {"Analyze the ethical implications of social media and digital privacy.", "SHORT_ANSWER", null,
                        new Object[][]{{"Social media raises concerns about privacy, autonomy, manipulation, and the impact on social relationships and democracy.", true}}},
                    {"What is the difference between moral constructivism and moral realism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral constructivism holds that moral facts are constructed by rational agents, while moral realism holds they exist independently", true}, {"Moral constructivism is about intentions, while moral realism is about consequences", false}, {"Moral constructivism is about rights, while moral realism is about duties", false}, {"Moral constructivism is about virtue, while moral realism is about character", false}}},
                    {"Explain the concept of moral luck in the context of criminal justice.", "SHORT_ANSWER", null,
                        new Object[][]{{"Moral luck in criminal justice raises questions about responsibility, punishment, and the role of chance in moral assessment.", true}}},
                    {"What is the difference between act deontology and rule deontology?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Act deontology evaluates individual actions, while rule deontology evaluates rules of action", true}, {"Act deontology is about intentions, while rule deontology is about consequences", false}, {"Act deontology is about rights, while rule deontology is about duties", false}, {"Act deontology is about virtue, while rule deontology is about character", false}}},
                    {"Analyze the ethical implications of automation and job displacement.", "SHORT_ANSWER", null,
                        new Object[][]{{"Automation raises concerns about distributive justice, human dignity, and the ethical responsibilities of technological development.", true}}},
                    {"What is the difference between moral internalism and moral externalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral internalism holds that moral judgments motivate action, while moral externalism denies this", true}, {"Moral internalism is about intentions, while moral externalism is about consequences", false}, {"Moral internalism is about rights, while moral externalism is about duties", false}, {"Moral internalism is about virtue, while moral externalism is about character", false}}},
                    {"Explain the concept of moral responsibility in the context of climate change.", "SHORT_ANSWER", null,
                        new Object[][]{{"Climate change responsibility involves questions about collective action, historical responsibility, and the distribution of burdens and benefits.", true}}},
                    {"What is the difference between ethical subjectivism and ethical objectivism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ethical subjectivism holds that moral judgments are based on individual attitudes, while ethical objectivism holds they are based on objective facts", true}, {"Ethical subjectivism is about intentions, while ethical objectivism is about consequences", false}, {"Ethical subjectivism is about rights, while ethical objectivism is about duties", false}, {"Ethical subjectivism is about virtue, while ethical objectivism is about character", false}}},
                    {"Analyze the ethical implications of artificial intelligence in healthcare.", "SHORT_ANSWER", null,
                        new Object[][]{{"AI in healthcare raises concerns about patient autonomy, privacy, bias, and the role of human judgment in medical decisions.", true}}},
                    {"What is the difference between moral rationalism and moral sentimentalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Moral rationalism holds that moral judgments are based on reason, while moral sentimentalism holds they are based on emotions", true}, {"Moral rationalism is about intentions, while moral sentimentalism is about consequences", false}, {"Moral rationalism is about rights, while moral sentimentalism is about duties", false}, {"Moral rationalism is about virtue, while moral sentimentalism is about character", false}}},
                    {"Explain the concept of moral responsibility in the context of artificial intelligence.", "SHORT_ANSWER", null,
                        new Object[][]{{"AI responsibility involves questions about agency, control, and the distribution of responsibility between humans and machines.", true}}}
                });

            // Add new subjects with Easy quizzes
            // UGRD-ENGL6100 Purposive Communication 2
            addQuizWithQuestions(conn, "Purposive Comm - Easy", "UGRD-ENGL6100 Purposive Communication 2", "EASY", 10,
                new Object[][]{
                    {"What is the primary goal of purposive communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To inform or persuade", true}, {"To entertain", false}, {"To confuse", false}}},
                    {"Is non-verbal communication important in purposive communication?", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"Clarity is one of the 7 Cs of effective communication.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"'Kinesics' refers to communication through...", "MULTIPLE_CHOICE", null, new Object[][]{{"Body movement", true}, {"Touch", false}, {"Time", false}, {"Vocal tone", false}}},
                    {"What is 'active listening'?", "SHORT_ANSWER", null, new Object[][]{{"Fully concentrating on what is being said rather than just passively hearing the message", true}}},
                    {"An impromptu speech is one that is delivered without prior preparation.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"'Ethos' in rhetoric refers to an appeal to...", "SHORT_ANSWER", null, new Object[][]{{"Ethics or credibility", true}}},
                    {"A formal business letter is an example of what type of communication?", "MULTIPLE_CHOICE", null, new Object[][]{{"Written communication", true}, {"Verbal communication", false}, {"Non-verbal communication", false}, {"Grapevine communication", false}}},
                    {"'Conciseness' means being long-winded in your message.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}},
                    {"What is a 'rhetorical question'?", "SHORT_ANSWER", null, new Object[][]{{"A question asked for effect or to make a point rather than to get an answer", true}}}
                });
            addQuizWithQuestions(conn, "Purposive Comm - Medium", "UGRD-ENGL6100 Purposive Communication 2", "MEDIUM", 20,
                new Object[][]{
                    {"What is a 'communication barrier'?", "SHORT_ANSWER", null,
                        new Object[][]{{"Anything that prevents understanding of the message", true}}},
                    {"Which of these is a form of written communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Email", true}, {"Handshake", false}, {"Speech", false}, {"Tone of voice", false}}},
                    {"What is the difference between verbal and non-verbal communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Verbal communication uses words, while non-verbal communication uses body language and gestures", true}, {"Verbal communication is always written, while non-verbal is always spoken", false}, {"Verbal communication is formal, while non-verbal is informal", false}, {"Verbal communication is professional, while non-verbal is personal", false}}},
                    {"What is the purpose of a communication audit?", "SHORT_ANSWER", null,
                        new Object[][]{{"To evaluate the effectiveness of communication within an organization", true}}},
                    {"What is the difference between formal and informal communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Formal communication follows established channels, while informal communication is more casual and flexible", true}, {"Formal communication is always written, while informal is always spoken", false}, {"Formal communication is for business, while informal is for personal use", false}, {"Formal communication is professional, while informal is unprofessional", false}}},
                    {"What is the concept of communication climate?", "SHORT_ANSWER", null,
                        new Object[][]{{"The emotional atmosphere created by communication patterns in a relationship or organization", true}}},
                    {"What is the difference between active and passive listening?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Active listening involves engagement and feedback, while passive listening is just hearing", true}, {"Active listening is for formal situations, while passive is for informal", false}, {"Active listening is for business, while passive is for personal", false}, {"Active listening is for leaders, while passive is for followers", false}}},
                    {"What is the purpose of a communication strategy?", "SHORT_ANSWER", null,
                        new Object[][]{{"To plan and implement effective communication to achieve specific goals", true}}},
                    {"What is the difference between synchronous and asynchronous communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Synchronous communication happens in real-time, while asynchronous communication has a time delay", true}, {"Synchronous is for business, while asynchronous is for personal", false}, {"Synchronous is formal, while asynchronous is informal", false}, {"Synchronous is written, while asynchronous is spoken", false}}},
                    {"What is the concept of communication competence?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ability to communicate effectively and appropriately in various contexts", true}}},
                    {"What is the difference between intrapersonal and interpersonal communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Intrapersonal communication is with oneself, while interpersonal is between people", true}, {"Intrapersonal is formal, while interpersonal is informal", false}, {"Intrapersonal is written, while interpersonal is spoken", false}, {"Intrapersonal is for business, while interpersonal is for personal", false}}},
                    {"What is the purpose of feedback in communication?", "SHORT_ANSWER", null,
                        new Object[][]{{"To confirm understanding and improve future communication", true}}},
                    {"What is the difference between mass communication and interpersonal communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Mass communication reaches many people, while interpersonal is between individuals", true}, {"Mass communication is formal, while interpersonal is informal", false}, {"Mass communication is written, while interpersonal is spoken", false}, {"Mass communication is for business, while interpersonal is for personal", false}}},
                    {"What is the concept of communication apprehension?", "SHORT_ANSWER", null,
                        new Object[][]{{"Fear or anxiety associated with communication situations", true}}},
                    {"What is the difference between verbal and written communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Verbal communication is spoken, while written communication is text-based", true}, {"Verbal is formal, while written is informal", false}, {"Verbal is for business, while written is for personal", false}, {"Verbal is professional, while written is casual", false}}},
                    {"What is the purpose of a communication plan?", "SHORT_ANSWER", null,
                        new Object[][]{{"To outline how information will be shared and with whom", true}}},
                    {"What is the difference between formal and informal writing?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Formal writing follows strict rules, while informal writing is more relaxed", true}, {"Formal writing is for business, while informal is for personal", false}, {"Formal writing is longer, while informal is shorter", false}, {"Formal writing is typed, while informal is handwritten", false}}},
                    {"What is the concept of communication ethics?", "SHORT_ANSWER", null,
                        new Object[][]{{"The moral principles that guide communication behavior", true}}},
                    {"What is the difference between public speaking and conversation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Public speaking is one-way communication to many, while conversation is two-way between few", true}, {"Public speaking is formal, while conversation is informal", false}, {"Public speaking is for business, while conversation is for personal", false}, {"Public speaking is written, while conversation is spoken", false}}},
                    {"What is the purpose of a communication channel?", "SHORT_ANSWER", null,
                        new Object[][]{{"To provide a medium for transmitting messages between sender and receiver", true}}},
                    {"What is the difference between direct and indirect communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Direct communication is explicit, while indirect communication is implicit", true}, {"Direct is formal, while indirect is informal", false}, {"Direct is for business, while indirect is for personal", false}, {"Direct is written, while indirect is spoken", false}}},
                    {"What is the concept of communication style?", "SHORT_ANSWER", null,
                        new Object[][]{{"The way a person typically communicates with others", true}}},
                    {"What is the difference between informative and persuasive communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Informative communication shares information, while persuasive communication aims to change attitudes or behavior", true}, {"Informative is formal, while persuasive is informal", false}, {"Informative is for business, while persuasive is for personal", false}, {"Informative is written, while persuasive is spoken", false}}},
                    {"What is the purpose of a communication audit?", "SHORT_ANSWER", null,
                        new Object[][]{{"To evaluate the effectiveness of communication within an organization", true}}},
                    {"What is the difference between verbal and non-verbal cues?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Verbal cues are words, while non-verbal cues are body language and gestures", true}, {"Verbal cues are formal, while non-verbal are informal", false}, {"Verbal cues are for business, while non-verbal are for personal", false}, {"Verbal cues are written, while non-verbal are spoken", false}}},
                    {"What is the concept of communication context?", "SHORT_ANSWER", null,
                        new Object[][]{{"The situation or environment in which communication takes place", true}}},
                    {"What is the difference between formal and informal presentation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Formal presentation follows strict structure, while informal is more relaxed", true}, {"Formal is for business, while informal is for personal", false}, {"Formal is longer, while informal is shorter", false}, {"Formal is with slides, while informal is without", false}}},
                    {"What is the purpose of a communication strategy?", "SHORT_ANSWER", null,
                        new Object[][]{{"To plan and implement effective communication to achieve specific goals", true}}},
                    {"What is the difference between synchronous and asynchronous communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Synchronous communication happens in real-time, while asynchronous has a time delay", true}, {"Synchronous is formal, while asynchronous is informal", false}, {"Synchronous is for business, while asynchronous is for personal", false}, {"Synchronous is written, while asynchronous is spoken", false}}},
                    {"What is the concept of communication competence?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ability to communicate effectively and appropriately in various contexts", true}}}
                });
            addQuizWithQuestions(conn, "Purposive Comm - Hard", "UGRD-ENGL6100 Purposive Communication 2", "HARD", 30,
                new Object[][]{
                    {"Analyze the role of cultural context in intercultural communication.", "SHORT_ANSWER", null,
                        new Object[][]{{"Cultural context shapes norms, values, and language, affecting interpretation and understanding.", true}}},
                    {"Public speaking is a one-way communication process.", "TRUE_FALSE", null,
                        new Object[][]{{"False", true}, {"True", false}}},
                    {"Analyze the impact of digital media on interpersonal communication.", "SHORT_ANSWER", null,
                        new Object[][]{{"Digital media has transformed communication patterns, affecting intimacy, immediacy, and the nature of social relationships.", true}}},
                    {"What is the difference between high-context and low-context communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"High-context relies on implicit meaning and context, while low-context relies on explicit verbal messages", true}, {"High-context is formal, while low-context is informal", false}, {"High-context is written, while low-context is spoken", false}, {"High-context is for business, while low-context is for personal use", false}}},
                    {"Explain the concept of communication accommodation theory.", "SHORT_ANSWER", null,
                        new Object[][]{{"The theory explains how people adjust their communication style to match or differentiate from others, affecting social distance and relationships.", true}}},
                    {"What is the difference between synchronous and asynchronous communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Synchronous happens in real-time, while asynchronous has a time delay", true}, {"Synchronous is formal, while asynchronous is informal", false}, {"Synchronous is for business, while asynchronous is for personal use", false}, {"Synchronous is written, while asynchronous is spoken", false}}},
                    {"Analyze the role of power dynamics in organizational communication.", "SHORT_ANSWER", null,
                        new Object[][]{{"Power dynamics influence message flow, decision-making, and the effectiveness of communication in organizational settings.", true}}},
                    {"What is the difference between formal and informal communication networks?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Formal networks follow official channels, while informal networks (grapevine) operate through social relationships", true}, {"Formal networks are faster, while informal networks are slower", false}, {"Formal networks are digital, while informal networks are face-to-face", false}, {"Formal networks are for managers, while informal networks are for employees", false}}},
                    {"Explain the concept of communication climate in organizations.", "SHORT_ANSWER", null,
                        new Object[][]{{"Communication climate refers to the emotional atmosphere created by communication patterns, affecting trust, openness, and organizational effectiveness.", true}}},
                    {"What is the difference between upward and downward communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Upward flows from subordinates to superiors, while downward flows from superiors to subordinates", true}, {"Upward is formal, while downward is informal", false}, {"Upward is written, while downward is spoken", false}, {"Upward is for feedback, while downward is for instructions", false}}},
                    {"Analyze the impact of technology on business communication.", "SHORT_ANSWER", null,
                        new Object[][]{{"Technology has transformed business communication through instant messaging, video conferencing, and social media, affecting speed, reach, and relationship building.", true}}},
                    {"What is the difference between internal and external communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Internal is within an organization, while external is with outside stakeholders", true}, {"Internal is formal, while external is informal", false}, {"Internal is written, while external is spoken", false}, {"Internal is for employees, while external is for customers", false}}},
                    {"Explain the concept of communication barriers in cross-cultural settings.", "SHORT_ANSWER", null,
                        new Object[][]{{"Cross-cultural barriers include language differences, non-verbal misinterpretations, and cultural assumptions that can hinder effective communication.", true}}},
                    {"What is the difference between linear and transactional communication models?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Linear is one-way, while transactional involves simultaneous sending and receiving", true}, {"Linear is formal, while transactional is informal", false}, {"Linear is written, while transactional is spoken", false}, {"Linear is for business, while transactional is for personal use", false}}},
                    {"Analyze the role of non-verbal communication in professional settings.", "SHORT_ANSWER", null,
                        new Object[][]{{"Non-verbal cues in professional settings affect credibility, rapport, and the interpretation of messages, often more than verbal content.", true}}},
                    {"What is the difference between informative and persuasive communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Informative shares information, while persuasive aims to change attitudes or behavior", true}, {"Informative is formal, while persuasive is informal", false}, {"Informative is written, while persuasive is spoken", false}, {"Informative is for business, while persuasive is for personal use", false}}},
                    {"Explain the concept of communication competence in intercultural settings.", "SHORT_ANSWER", null,
                        new Object[][]{{"Intercultural communication competence involves cultural awareness, adaptability, and the ability to communicate effectively across cultural boundaries.", true}}},
                    {"What is the difference between mass communication and interpersonal communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Mass communication reaches many people, while interpersonal is between individuals", true}, {"Mass communication is formal, while interpersonal is informal", false}, {"Mass communication is written, while interpersonal is spoken", false}, {"Mass communication is for business, while interpersonal is for personal use", false}}},
                    {"Analyze the impact of social media on organizational communication.", "SHORT_ANSWER", null,
                        new Object[][]{{"Social media has transformed organizational communication through transparency, engagement, and the blurring of personal and professional boundaries.", true}}},
                    {"What is the difference between formal and informal writing?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Formal writing follows strict rules, while informal writing is more relaxed", true}, {"Formal writing is for business, while informal is for personal use", false}, {"Formal writing is longer, while informal is shorter", false}, {"Formal writing is typed, while informal is handwritten", false}}},
                    {"Explain the concept of communication ethics in digital media.", "SHORT_ANSWER", null,
                        new Object[][]{{"Digital communication ethics involves issues of privacy, authenticity, and responsible use of technology in communication.", true}}},
                    {"What is the difference between public speaking and conversation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Public speaking is one-way to many, while conversation is two-way between few", true}, {"Public speaking is formal, while conversation is informal", false}, {"Public speaking is for business, while conversation is for personal use", false}, {"Public speaking is written, while conversation is spoken", false}}},
                    {"Analyze the role of feedback in effective communication.", "SHORT_ANSWER", null,
                        new Object[][]{{"Feedback is crucial for message clarification, relationship building, and improving communication effectiveness.", true}}},
                    {"What is the difference between direct and indirect communication?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Direct communication is explicit, while indirect communication is implicit", true}, {"Direct is formal, while indirect is informal", false}, {"Direct is for business, while indirect is for personal use", false}, {"Direct is written, while indirect is spoken", false}}},
                    {"Explain the concept of communication style in professional settings.", "SHORT_ANSWER", null,
                        new Object[][]{{"Professional communication style involves adapting language, tone, and approach to different contexts and audiences.", true}}},
                    {"What is the difference between informative and persuasive presentation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Informative presentation shares information, while persuasive presentation aims to change attitudes or behavior", true}, {"Informative is formal, while persuasive is informal", false}, {"Informative is for business, while persuasive is for personal use", false}, {"Informative is written, while persuasive is spoken", false}}},
                    {"Analyze the impact of technology on public speaking.", "SHORT_ANSWER", null,
                        new Object[][]{{"Technology has transformed public speaking through visual aids, virtual presentations, and new audience engagement methods.", true}}},
                    {"What is the difference between formal and informal presentation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Formal presentation follows strict structure, while informal is more relaxed", true}, {"Formal is for business, while informal is for personal use", false}, {"Formal is longer, while informal is shorter", false}, {"Formal is with slides, while informal is without", false}}},
                    {"Explain the concept of communication strategy in crisis management.", "SHORT_ANSWER", null,
                        new Object[][]{{"Crisis communication strategy involves timely, transparent, and consistent messaging to maintain trust and manage reputation.", true}}}
                });

            // UGRD-ETHNS6102 Euthenics 2
            addQuizWithQuestions(conn, "Euthenics 2 - Easy", "UGRD-ETHNS6102 Euthenics 2", "EASY", 10,
                new Object[][]{
                    {"What does Euthenics primarily focus on?", "SHORT_ANSWER", null,
                        new Object[][]{{"Improving human well-being through environmental factors", true}}},
                    {"Does Euthenics involve genetic engineering?", "TRUE_FALSE", null,
                        new Object[][]{{"False", true}, {"True", false}}},
                    {"Euthenics believes that human improvement comes from improving living conditions.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"Which of these is a key area of focus for Euthenics?", "MULTIPLE_CHOICE", null, new Object[][]{{"Sanitation", true}, {"Astrology", false}, {"Gene editing", false}, {"Palmistry", false}}},
                    {"What is the term for the study of human improvement by genetic means?", "SHORT_ANSWER", null, new Object[][]{{"Eugenics", true}}},
                    {"A well-planned urban environment can contribute positively to human well-being.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"Euthenics is considered a pseudoscience by some critics.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"The word 'Euthenics' comes from a Greek word meaning...", "SHORT_ANSWER", null, new Object[][]{{"To cause to thrive", true}}},
                    {"Improving access to quality education is a principle of Euthenics.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"Which of the following would NOT be a concern of Euthenics?", "MULTIPLE_CHOICE", null, new Object[][]{{"Selecting for desirable genetic traits", true}, {"Improving public parks", false}, {"Ensuring clean drinking water", false}, {"Promoting better nutrition", false}}}
                });
            addQuizWithQuestions(conn, "Euthenics 2 - Medium", "UGRD-ETHNS6102 Euthenics 2", "MEDIUM", 20,
                new Object[][]{
                    {"What is the main difference between Eugenics and Euthenics?", "SHORT_ANSWER", null,
                        new Object[][]{{"Eugenics is about improving heredity, while Euthenics is about improving the environment.", true}}},
                    {"Improving public sanitation is an example of a Euthenics principle.", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"What is the relationship between Euthenics and public health?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics provides the theoretical framework for many public health initiatives", true}, {"They are completely unrelated fields", false}, {"Public health is a subset of Euthenics", false}, {"Euthenics is a subset of public health", false}}},
                    {"How does Euthenics approach urban planning?", "SHORT_ANSWER", null,
                        new Object[][]{{"By designing cities to promote physical and mental well-being through environmental factors", true}}},
                    {"What is the difference between Euthenics and environmental science?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics focuses on human improvement through environment, while environmental science studies the environment itself", true}, {"They are exactly the same field", false}, {"Environmental science is more important than Euthenics", false}, {"Euthenics is more scientific than environmental science", false}}},
                    {"What role does nutrition play in Euthenics?", "SHORT_ANSWER", null,
                        new Object[][]{{"It's a key factor in improving human well-being through environmental means", true}}},
                    {"How does Euthenics view education?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As an environmental factor that can improve human development", true}, {"As unrelated to human improvement", false}, {"As less important than genetics", false}, {"As the only factor in human development", false}}},
                    {"What is the relationship between Euthenics and social reform?", "SHORT_ANSWER", null,
                        new Object[][]{{"Euthenics provides a framework for improving society through environmental changes", true}}},
                    {"What is the difference between Euthenics and social engineering?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics focuses on improving conditions, while social engineering focuses on controlling behavior", true}, {"They are exactly the same", false}, {"Social engineering is more scientific", false}, {"Euthenics is more manipulative", false}}},
                    {"How does Euthenics approach housing design?", "SHORT_ANSWER", null,
                        new Object[][]{{"By creating living spaces that promote physical and mental well-being", true}}},
                    {"What is the role of technology in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As a tool for improving living conditions and human well-being", true}, {"As something to be avoided", false}, {"As the only solution to human problems", false}, {"As unrelated to human improvement", false}}},
                    {"What is the relationship between Euthenics and public policy?", "SHORT_ANSWER", null,
                        new Object[][]{{"Euthenics can inform policies aimed at improving living conditions", true}}},
                    {"How does Euthenics view healthcare?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As part of a broader approach to improving human well-being through environmental factors", true}, {"As the only way to improve human health", false}, {"As unrelated to human improvement", false}, {"As less important than genetic factors", false}}},
                    {"What is the role of community planning in Euthenics?", "SHORT_ANSWER", null,
                        new Object[][]{{"To create environments that promote social interaction and well-being", true}}},
                    {"What is the difference between Euthenics and public health?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics is broader, encompassing all environmental factors, while public health focuses on health specifically", true}, {"They are exactly the same", false}, {"Public health is broader than Euthenics", false}, {"They are completely unrelated", false}}},
                    {"How does Euthenics approach workplace design?", "SHORT_ANSWER", null,
                        new Object[][]{{"By creating work environments that promote productivity and well-being", true}}},
                    {"What is the relationship between Euthenics and urban development?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics provides principles for creating healthier urban environments", true}, {"They are unrelated", false}, {"Urban development is more important", false}, {"Euthenics is only about rural areas", false}}},
                    {"What is the role of recreation in Euthenics?", "SHORT_ANSWER", null,
                        new Object[][]{{"As an environmental factor that promotes physical and mental well-being", true}}},
                    {"How does Euthenics view transportation systems?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As environmental factors that can improve or hinder human well-being", true}, {"As unrelated to human development", false}, {"As the most important factor", false}, {"As something to be avoided", false}}},
                    {"What is the relationship between Euthenics and architecture?", "SHORT_ANSWER", null,
                        new Object[][]{{"Euthenics can inform architectural design to promote human well-being", true}}},
                    {"What is the role of green spaces in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As important environmental factors for human well-being", true}, {"As purely aesthetic elements", false}, {"As unnecessary luxuries", false}, {"As unrelated to human development", false}}},
                    {"How does Euthenics approach noise pollution?", "SHORT_ANSWER", null,
                        new Object[][]{{"As an environmental factor that can be managed to improve human well-being", true}}},
                    {"What is the relationship between Euthenics and social welfare?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics can inform social welfare policies to improve living conditions", true}, {"They are unrelated", false}, {"Social welfare is more important", false}, {"Euthenics is only about physical environment", false}}},
                    {"What is the role of air quality in Euthenics?", "SHORT_ANSWER", null,
                        new Object[][]{{"As a crucial environmental factor affecting human health and well-being", true}}},
                    {"How does Euthenics view water quality?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As a fundamental environmental factor for human well-being", true}, {"As unrelated to human development", false}, {"As less important than other factors", false}, {"As something to be ignored", false}}},
                    {"What is the relationship between Euthenics and public safety?", "SHORT_ANSWER", null,
                        new Object[][]{{"Euthenics can inform approaches to creating safer environments", true}}},
                    {"What is the role of lighting in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"As an environmental factor that affects human well-being and productivity", true}, {"As purely aesthetic", false}, {"As unimportant", false}, {"As unrelated to human development", false}}},
                    {"How does Euthenics approach waste management?", "SHORT_ANSWER", null,
                        new Object[][]{{"As an environmental factor that can be managed to improve living conditions", true}}},
                    {"What is the relationship between Euthenics and public transportation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics can inform the design of transportation systems to improve human well-being", true}, {"They are unrelated", false}, {"Public transportation is more important", false}, {"Euthenics is only about private transportation", false}}},
                    {"What is the role of temperature control in Euthenics?", "SHORT_ANSWER", null,
                        new Object[][]{{"As an environmental factor that affects human comfort and productivity", true}}}
                });
            addQuizWithQuestions(conn, "Euthenics 2 - Hard", "UGRD-ETHNS6102 Euthenics 2", "HARD", 30,
                new Object[][]{
                    {"Discuss the ethical implications of applying Euthenics at a societal level.", "SHORT_ANSWER", null,
                        new Object[][]{{"It raises concerns about social engineering and individual freedom.", true}}},
                    {"Which factor is NOT a primary focus of Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Genetic modification", true}, {"Education", false}, {"Nutrition", false}, {"Housing", false}}},
                    {"Analyze the relationship between urban planning and human well-being.", "SHORT_ANSWER", null,
                        new Object[][]{{"Urban planning affects physical and mental health through factors like green spaces, walkability, and social interaction opportunities.", true}}},
                    {"What is the difference between Euthenics and environmental psychology?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics focuses on improving human well-being through environment, while environmental psychology studies the relationship between people and their environment", true}, {"They are exactly the same", false}, {"Environmental psychology is more important", false}, {"Euthenics is more scientific", false}}},
                    {"Explain the concept of environmental determinism in Euthenics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental determinism suggests that physical and social environments significantly influence human development and behavior.", true}}},
                    {"What is the role of technology in modern Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To improve living conditions and human well-being through environmental modifications", true}, {"To replace natural environments", false}, {"To control human behavior", false}, {"To eliminate environmental factors", false}}},
                    {"Analyze the impact of housing design on human development.", "SHORT_ANSWER", null,
                        new Object[][]{{"Housing design affects physical health, mental well-being, and social interaction through factors like space, light, and layout.", true}}},
                    {"What is the difference between Euthenics and public health?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics is broader, encompassing all environmental factors, while public health focuses on health specifically", true}, {"They are exactly the same", false}, {"Public health is broader", false}, {"They are unrelated", false}}},
                    {"Explain the concept of environmental justice in Euthenics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental justice ensures fair distribution of environmental benefits and burdens across different social groups.", true}}},
                    {"What is the role of education in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To improve human development through better learning environments and methods", true}, {"To control human behavior", false}, {"To replace environmental factors", false}, {"To eliminate social differences", false}}},
                    {"Analyze the relationship between nutrition and human development.", "SHORT_ANSWER", null,
                        new Object[][]{{"Nutrition affects physical growth, cognitive development, and overall well-being, making it a key factor in Euthenics.", true}}},
                    {"What is the difference between Euthenics and social engineering?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics focuses on improving conditions, while social engineering focuses on controlling behavior", true}, {"They are exactly the same", false}, {"Social engineering is more scientific", false}, {"Euthenics is more manipulative", false}}},
                    {"Explain the concept of environmental sustainability in Euthenics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental sustainability ensures that improvements to human well-being don't compromise future generations' ability to meet their needs.", true}}},
                    {"What is the role of recreation in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To promote physical and mental well-being through environmental design", true}, {"To control leisure time", false}, {"To replace work", false}, {"To eliminate stress", false}}},
                    {"Analyze the impact of transportation systems on human well-being.", "SHORT_ANSWER", null,
                        new Object[][]{{"Transportation systems affect physical health, social interaction, and environmental quality through factors like accessibility and pollution.", true}}},
                    {"What is the difference between Euthenics and urban sociology?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics focuses on improving human well-being through environment, while urban sociology studies urban social structures", true}, {"They are exactly the same", false}, {"Urban sociology is more important", false}, {"Euthenics is more theoretical", false}}},
                    {"Explain the concept of environmental psychology in Euthenics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental psychology studies how physical and social environments affect human behavior and well-being.", true}}},
                    {"What is the role of green spaces in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To improve physical and mental well-being through environmental design", true}, {"To control nature", false}, {"To replace buildings", false}, {"To eliminate pollution", false}}},
                    {"Analyze the relationship between workplace design and productivity.", "SHORT_ANSWER", null,
                        new Object[][]{{"Workplace design affects productivity through factors like lighting, air quality, and social interaction spaces.", true}}},
                    {"What is the difference between Euthenics and architecture?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics provides principles for human well-being, while architecture focuses on building design", true}, {"They are exactly the same", false}, {"Architecture is more important", false}, {"They are unrelated", false}}},
                    {"Explain the concept of environmental health in Euthenics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental health focuses on how physical and social environments affect human health and well-being.", true}}},
                    {"What is the role of community planning in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To create environments that promote social interaction and well-being", true}, {"To control communities", false}, {"To replace nature", false}, {"To eliminate differences", false}}},
                    {"Analyze the impact of noise pollution on human development.", "SHORT_ANSWER", null,
                        new Object[][]{{"Noise pollution affects physical health, mental well-being, and cognitive development through stress and sleep disruption.", true}}},
                    {"What is the difference between Euthenics and public policy?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics provides principles for improving conditions, while public policy implements these principles", true}, {"They are exactly the same", false}, {"Public policy is more important", false}, {"They are unrelated", false}}},
                    {"Explain the concept of environmental design in Euthenics.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental design creates spaces that promote human well-being through factors like layout, materials, and natural elements.", true}}},
                    {"What is the role of air quality in Euthenics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To ensure healthy living conditions through environmental management", true}, {"To control breathing", false}, {"To replace ventilation", false}, {"To eliminate pollution", false}}},
                    {"Analyze the relationship between lighting and human well-being.", "SHORT_ANSWER", null,
                        new Object[][]{{"Lighting affects physical health, mental well-being, and productivity through factors like natural light, intensity, and color temperature.", true}}},
                    {"What is the difference between Euthenics and environmental science?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Euthenics focuses on human improvement through environment, while environmental science studies the environment itself", true}, {"They are exactly the same", false}, {"Environmental science is more important", false}, {"They are unrelated", false}}},
                    {"Explain the concept of environmental sustainability in urban development.", "SHORT_ANSWER", null,
                        new Object[][]{{"Environmental sustainability in urban development ensures that cities meet current needs without compromising future generations' ability to meet their needs.", true}}}
                });
            
            // UGRD-GE6102 The Contemporary World
            addQuizWithQuestions(conn, "Contemporary World - Easy", "UGRD-GE6102 The Contemporary World", "EASY", 10,
                new Object[][]{
                    {"What is globalization?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"The increasing interconnectedness of countries", true}, {"A type of local market", false}, {"A historical period", false}}},
                    {"The United Nations is an example of a global institution.", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"The International Monetary Fund (IMF) is a global organization that works to foster global monetary cooperation.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"'Glocalization' refers to...", "MULTIPLE_CHOICE", null, new Object[][]{{"The adaptation of global products to local tastes", true}, {"The end of globalization", false}, {"A focus on only local issues", false}, {"A new form of government", false}}},
                    {"What is 'global citizenship'?", "SHORT_ANSWER", null, new Object[][]{{"The idea that one's identity transcends geography or political borders", true}}},
                    {"Sustainable development aims to meet the needs of the present without compromising the ability of future generations to meet their own needs.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"Which of these is a major driver of globalization?", "SHORT_ANSWER", null, new Object[][]{{"Technology", true}}},
                    {"A 'nation-state' is a political unit where the state and nation are congruent.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What does the term 'North-South divide' refer to?", "SHORT_ANSWER", null, new Object[][]{{"The socio-economic and political division between wealthy developed countries and poorer developing countries", true}}},
                    {"Transnational corporations operate in only one country.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}}
                });
            addQuizWithQuestions(conn, "Contemporary World - Medium", "UGRD-GE6102 The Contemporary World", "MEDIUM", 20,
                new Object[][]{
                    {"What is 'cultural homogenization'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The process whereby local cultures are transformed or absorbed by a dominant outside culture.", true}}},
                    {"Economic globalization is characterized by...", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Increased free trade and capital mobility", true}, {"Strict government control on all industries", false}, {"The decline of multinational corporations", false}, {"Isolationist trade policies", false}}},
                    {"What is the 'digital divide'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The gap between those who have access to digital technology and those who don't", true}}},
                    {"What is the role of the World Bank in global development?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To provide financial and technical assistance to developing countries", true}, {"To regulate global trade", false}, {"To enforce international law", false}, {"To manage global currency", false}}},
                    {"What is 'soft power' in international relations?", "SHORT_ANSWER", null,
                        new Object[][]{{"The ability to influence others through attraction and persuasion rather than coercion", true}}},
                    {"What is the difference between globalization and internationalization?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Globalization involves integration of economies and societies, while internationalization involves cooperation between nations", true}, {"They are exactly the same", false}, {"Globalization is about trade, while internationalization is about culture", false}, {"Globalization is political, while internationalization is economic", false}}},
                    {"What is the concept of 'global governance'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The way in which global affairs are managed in the absence of a world government", true}}},
                    {"What is the role of NGOs in global affairs?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To address social and political issues across borders", true}, {"To regulate international trade", false}, {"To enforce international law", false}, {"To manage global currency", false}}},
                    {"What is 'cultural imperialism'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The practice of promoting and imposing one culture over another", true}}},
                    {"What is the difference between developed and developing countries?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Developed countries have higher standards of living and more advanced economies", true}, {"They are exactly the same", false}, {"Developing countries are more technologically advanced", false}, {"Developed countries have larger populations", false}}},
                    {"What is the concept of 'sustainable development'?", "SHORT_ANSWER", null,
                        new Object[][]{{"Development that meets present needs without compromising future generations", true}}},
                    {"What is the role of the United Nations in global affairs?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To maintain international peace and security and promote cooperation", true}, {"To regulate global trade", false}, {"To manage global currency", false}, {"To enforce international law", false}}},
                    {"What is 'global citizenship'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The idea that one's identity transcends geography or political borders", true}}},
                    {"What is the difference between globalism and nationalism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Globalism emphasizes international cooperation, while nationalism emphasizes national interests", true}, {"They are exactly the same", false}, {"Globalism is about trade, while nationalism is about culture", false}, {"Globalism is political, while nationalism is economic", false}}},
                    {"What is the concept of 'global village'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The idea that the world has become more interconnected through technology and communication", true}}},
                    {"What is the role of multinational corporations in globalization?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To operate in multiple countries and drive economic integration", true}, {"To regulate international trade", false}, {"To enforce international law", false}, {"To manage global currency", false}}},
                    {"What is 'cultural relativism'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The principle that cultures should be understood on their own terms", true}}},
                    {"What is the difference between hard power and soft power?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Hard power uses coercion, while soft power uses attraction and persuasion", true}, {"They are exactly the same", false}, {"Hard power is economic, while soft power is military", false}, {"Hard power is political, while soft power is cultural", false}}},
                    {"What is the concept of 'global civil society'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The sphere of voluntary collective action around shared interests and values", true}}},
                    {"What is the role of international law in global governance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To provide a framework for international relations and cooperation", true}, {"To regulate global trade", false}, {"To manage global currency", false}, {"To enforce national laws", false}}},
                    {"What is 'economic interdependence'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The mutual dependence of countries on each other's economic activities", true}}},
                    {"What is the difference between globalization and regionalization?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Globalization involves worldwide integration, while regionalization involves integration within regions", true}, {"They are exactly the same", false}, {"Globalization is about trade, while regionalization is about culture", false}, {"Globalization is political, while regionalization is economic", false}}},
                    {"What is the concept of 'global public goods'?", "SHORT_ANSWER", null,
                        new Object[][]{{"Goods that benefit all countries and cannot be excluded from use", true}}},
                    {"What is the role of international organizations in global governance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To facilitate cooperation and address global issues", true}, {"To regulate global trade", false}, {"To manage global currency", false}, {"To enforce international law", false}}},
                    {"What is 'cultural hybridization'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The blending of different cultural elements to create new forms", true}}},
                    {"What is the difference between global governance and world government?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Global governance is decentralized, while world government would be centralized", true}, {"They are exactly the same", false}, {"Global governance is about trade, while world government is about culture", false}, {"Global governance is political, while world government is economic", false}}},
                    {"What is the concept of 'global commons'?", "SHORT_ANSWER", null,
                        new Object[][]{{"Resources that belong to all humanity and require collective management", true}}},
                    {"What is the role of technology in globalization?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To facilitate communication and economic integration across borders", true}, {"To regulate international trade", false}, {"To enforce international law", false}, {"To manage global currency", false}}},
                    {"What is 'cultural diversity'?", "SHORT_ANSWER", null,
                        new Object[][]{{"The existence of a variety of cultural or ethnic groups within a society", true}}},
                    {"What is the difference between global and local perspectives?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Global perspectives consider worldwide issues, while local perspectives focus on specific communities", true}, {"They are exactly the same", false}, {"Global perspectives are about trade, while local perspectives are about culture", false}, {"Global perspectives are political, while local perspectives are economic", false}}}
                });
            addQuizWithQuestions(conn, "Contemporary World - Hard", "UGRD-GE6102 The Contemporary World", "HARD", 30,
                new Object[][]{
                    {"Critique the argument that globalization leads to greater global inequality.", "SHORT_ANSWER", null,
                        new Object[][]{{"Critics argue it widens the gap between rich and poor nations and within nations, while proponents argue it lifts many out of poverty.", true}}},
                    {"The World Trade Organization (WTO) primarily deals with the rules of trade between nations.", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}}
                });

            // UGRD-GE6114 Mathematics in the Modern World
            addQuizWithQuestions(conn, "Math in Modern World - Easy", "UGRD-GE6114 Mathematics in the Modern World", "EASY", 10,
                new Object[][]{
                    {"Is mathematics only about numbers and calculations?", "TRUE_FALSE", null,
                        new Object[][]{{"False", true}, {"True", false}}}, // Math involves patterns, logic, etc.
                    {"What is a common application of Fibonacci sequence in nature?", "SHORT_ANSWER", null,
                        new Object[][]{{"Arrangement of leaves on a stem or petals on a flower", true}}},
                    {"What is a 'fractal'?", "SHORT_ANSWER", null, new Object[][]{{"A never-ending pattern that is self-similar across different scales", true}}},
                    {"Voting theory is a branch of mathematics.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What does 'exponential growth' describe?", "MULTIPLE_CHOICE", null, new Object[][]{{"Growth whose rate becomes ever more rapid in proportion to the growing total number or size", true}, {"Slow and steady growth", false}, {"Growth that stops after a certain point", false}, {"Negative growth", false}}},
                    {"Graph theory can be used to model social networks.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What is 'cryptography'?", "SHORT_ANSWER", null, new Object[][]{{"The practice and study of techniques for secure communication in the presence of third parties", true}}},
                    {"The concept of 'infinity' is not used in modern mathematics.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}},
                    {"What field of math is used to analyze risk and uncertainty?", "SHORT_ANSWER", null, new Object[][]{{"Probability", true}}},
                    {"A 'linear' relationship between two variables means they form a straight line when plotted on a graph.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}}
                });
            addQuizWithQuestions(conn, "Math in Modern World - Medium", "UGRD-GE6114 Mathematics in the Modern World", "MEDIUM", 20,
                new Object[][]{
                    {"How is mathematics used in cryptography?", "SHORT_ANSWER", null,
                        new Object[][]{{"It uses number theory and prime numbers to create secure encryption codes.", true}}},
                    {"Is the Golden Ratio related to the Fibonacci Sequence?", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"What is the relationship between mathematics and music?", "SHORT_ANSWER", null,
                        new Object[][]{{"Mathematics provides the foundation for musical scales, rhythm, and harmony", true}}},
                    {"How is probability used in weather forecasting?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To predict the likelihood of weather events based on historical data and current conditions", true}, {"To control the weather", false}, {"To measure temperature", false}, {"To create weather maps", false}}},
                    {"What is the role of mathematics in data analysis?", "SHORT_ANSWER", null,
                        new Object[][]{{"To process, analyze, and interpret large sets of data to find patterns and make predictions", true}}},
                    {"How is graph theory applied in social networks?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To model relationships and connections between individuals or groups", true}, {"To create social media platforms", false}, {"To store user data", false}, {"To design websites", false}}},
                    {"What is the significance of prime numbers in modern cryptography?", "SHORT_ANSWER", null,
                        new Object[][]{{"They are used to create secure encryption keys that are difficult to factor", true}}},
                    {"How is mathematics used in image processing?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To manipulate and enhance digital images using mathematical algorithms", true}, {"To take photographs", false}, {"To store images", false}, {"To display images", false}}},
                    {"What is the role of statistics in medical research?", "SHORT_ANSWER", null,
                        new Object[][]{{"To analyze clinical data and determine the effectiveness of treatments", true}}},
                    {"How is calculus used in physics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To model and analyze continuous change in physical systems", true}, {"To measure distance", false}, {"To calculate speed", false}, {"To determine mass", false}}},
                    {"What is the application of linear algebra in computer graphics?", "SHORT_ANSWER", null,
                        new Object[][]{{"To transform and manipulate 3D objects and create visual effects", true}}},
                    {"How is mathematics used in financial modeling?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To predict market trends and assess investment risks", true}, {"To print money", false}, {"To store financial data", false}, {"To design bank buildings", false}}},
                    {"What is the role of number theory in modern computing?", "SHORT_ANSWER", null,
                        new Object[][]{{"To develop algorithms and ensure data security", true}}},
                    {"How is mathematics applied in game theory?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To analyze strategic decision-making in competitive situations", true}, {"To create video games", false}, {"To design game controllers", false}, {"To program game graphics", false}}},
                    {"What is the significance of fractals in nature?", "SHORT_ANSWER", null,
                        new Object[][]{{"They describe complex patterns that repeat at different scales", true}}},
                    {"How is mathematics used in artificial intelligence?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To develop algorithms and models for machine learning", true}, {"To create robots", false}, {"To store data", false}, {"To design interfaces", false}}},
                    {"What is the role of probability in risk assessment?", "SHORT_ANSWER", null,
                        new Object[][]{{"To evaluate the likelihood and impact of potential events", true}}},
                    {"How is mathematics applied in cryptography?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To create secure communication systems and protect data", true}, {"To store passwords", false}, {"To design websites", false}, {"To create user accounts", false}}},
                    {"What is the relationship between mathematics and art?", "SHORT_ANSWER", null,
                        new Object[][]{{"Mathematics provides principles for composition, perspective, and symmetry", true}}},
                    {"How is statistics used in sports analytics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To analyze performance and develop strategies", true}, {"To keep score", false}, {"To design stadiums", false}, {"To create uniforms", false}}},
                    {"What is the role of mathematics in climate modeling?", "SHORT_ANSWER", null,
                        new Object[][]{{"To predict climate patterns and assess environmental changes", true}}},
                    {"How is mathematics applied in network security?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To develop encryption and security protocols", true}, {"To design networks", false}, {"To store data", false}, {"To create websites", false}}},
                    {"What is the significance of mathematical patterns in nature?", "SHORT_ANSWER", null,
                        new Object[][]{{"They reveal underlying order and structure in natural phenomena", true}}},
                    {"How is mathematics used in economic forecasting?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To predict economic trends and analyze market behavior", true}, {"To print money", false}, {"To design banks", false}, {"To create financial products", false}}},
                    {"What is the role of geometry in architecture?", "SHORT_ANSWER", null,
                        new Object[][]{{"To design structures and ensure stability", true}}},
                    {"How is mathematics applied in signal processing?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To analyze and manipulate signals in communication systems", true}, {"To create signals", false}, {"To store signals", false}, {"To display signals", false}}},
                    {"What is the relationship between mathematics and music theory?", "SHORT_ANSWER", null,
                        new Object[][]{{"Mathematics provides the foundation for scales, intervals, and harmony", true}}},
                    {"How is statistics used in quality control?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To monitor and improve product quality", true}, {"To design products", false}, {"To store products", false}, {"To sell products", false}}},
                    {"What is the role of mathematics in computer animation?", "SHORT_ANSWER", null,
                        new Object[][]{{"To create realistic movement and visual effects", true}}},
                    {"How is mathematics applied in voting systems?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To analyze voting patterns and ensure fair representation", true}, {"To count votes", false}, {"To design ballots", false}, {"To store results", false}}}
                });
            addQuizWithQuestions(conn, "Math in Modern World - Hard", "UGRD-GE6114 Mathematics in the Modern World", "HARD", 30,
                new Object[][]{
                    {"Explain how graph theory is used to model and solve problems like network routing.", "SHORT_ANSWER", null,
                        new Object[][]{{"It represents locations as vertices and connections as edges to find the shortest or most efficient path.", true}}},
                    {"Which of the following is NOT an application of statistics?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Determining the exact winning lottery numbers", true}, {"Political polling", false}, {"Medical studies", false}, {"Quality control in manufacturing", false}}}
                });

            // UGRD-NSTP6102 National Service Training Program 2
            addQuizWithQuestions(conn, "NSTP 2 - Easy", "UGRD-NSTP6102 National Service Training Program 2", "EASY", 10,
                new Object[][]{
                    {"What are the three components of NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"CWTS, LTS, and ROTC", true}}},
                    {"NSTP is a mandatory program for all college students in the Philippines.", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"LTS in NSTP stands for Literacy Training Service.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What is 'community organizing'?", "SHORT_ANSWER", null, new Object[][]{{"The process by which people come together to identify common problems or goals, mobilize resources, and develop and implement strategies", true}}},
                    {"Which government agency is the lead in implementing NSTP?", "MULTIPLE_CHOICE", null, new Object[][]{{"CHED", true}, {"DepEd", false}, {"DSWD", false}, {"DND", false}}},
                    {"Volunteerism is a core value promoted by the NSTP.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What is the purpose of ROTC?", "SHORT_ANSWER", null, new Object[][]{{"To provide military training to tertiary level students in order to motivate, train, organize and mobilize them for national defense preparedness", true}}},
                    {"Students who are part of the school's varsity team are exempt from NSTP.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}},
                    {"What is a 'needs assessment' in the context of community work?", "SHORT_ANSWER", null, new Object[][]{{"A systematic process for determining and addressing needs or gaps between current conditions and desired conditions", true}}},
                    {"The NSTP Act of 2001 is also known as Republic Act...", "SHORT_ANSWER", null, new Object[][]{{"9163", true}}}
                });
            addQuizWithQuestions(conn, "NSTP 2 - Medium", "UGRD-NSTP6102 National Service Training Program 2", "MEDIUM", 20,
                new Object[][]{
                    {"What is the main objective of the Civic Welfare Training Service (CWTS)?", "SHORT_ANSWER", null,
                        new Object[][]{{"To contribute to the general welfare and betterment of life for community members.", true}}},
                    {"Community immersion is a key requirement of the NSTP program.", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"What is the role of needs assessment in community development?", "SHORT_ANSWER", null,
                        new Object[][]{{"To identify community problems and resources for effective project planning", true}}},
                    {"How does the NSTP program contribute to nation-building?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"By developing civic consciousness and social responsibility among students", true}, {"By providing military training", false}, {"By offering academic credits", false}, {"By organizing sports events", false}}},
                    {"What is the importance of community mapping in NSTP projects?", "SHORT_ANSWER", null,
                        new Object[][]{{"To identify resources, problems, and opportunities in the community", true}}},
                    {"What is the difference between CWTS and LTS?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"CWTS focuses on community service, while LTS focuses on literacy training", true}, {"They are exactly the same", false}, {"CWTS is for men, LTS is for women", false}, {"CWTS is longer than LTS", false}}},
                    {"What is the role of project planning in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"To organize and implement community service activities effectively", true}}},
                    {"How does the NSTP program promote volunteerism?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"By engaging students in community service and social responsibility", true}, {"By providing financial incentives", false}, {"By offering academic credits", false}, {"By organizing competitions", false}}},
                    {"What is the importance of community participation in NSTP projects?", "SHORT_ANSWER", null,
                        new Object[][]{{"To ensure sustainability and relevance of community initiatives", true}}},
                    {"What is the role of documentation in NSTP projects?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To record project activities and outcomes for evaluation", true}, {"To provide entertainment", false}, {"To create social media content", false}, {"To design posters", false}}},
                    {"What is the concept of sustainable development in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"Creating long-lasting positive impact in communities through well-planned projects", true}}},
                    {"How does the NSTP program address social issues?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Through community service and awareness programs", true}, {"By providing financial aid", false}, {"By organizing protests", false}, {"By creating laws", false}}},
                    {"What is the importance of teamwork in NSTP projects?", "SHORT_ANSWER", null,
                        new Object[][]{{"To achieve project goals through collaborative effort", true}}},
                    {"What is the role of leadership in NSTP activities?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To guide and coordinate project implementation", true}, {"To provide funding", false}, {"To create rules", false}, {"To evaluate students", false}}},
                    {"What is the concept of community empowerment in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"Enabling communities to take control of their development", true}}},
                    {"How does the NSTP program promote environmental awareness?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Through environmental projects and education", true}, {"By creating parks", false}, {"By planting trees", false}, {"By cleaning streets", false}}},
                    {"What is the importance of project evaluation in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"To assess project effectiveness and identify areas for improvement", true}}},
                    {"What is the role of communication in NSTP projects?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To coordinate activities and engage stakeholders", true}, {"To create reports", false}, {"To design posters", false}, {"To take photos", false}}},
                    {"What is the concept of social responsibility in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"The obligation to act for the benefit of society", true}}},
                    {"How does the NSTP program contribute to personal development?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"By developing leadership and social skills", true}, {"By providing jobs", false}, {"By offering scholarships", false}, {"By organizing parties", false}}},
                    {"What is the importance of cultural sensitivity in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"To respect and understand community values and traditions", true}}},
                    {"What is the role of resource mobilization in NSTP?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To gather necessary resources for project implementation", true}, {"To create budgets", false}, {"To design posters", false}, {"To take photos", false}}},
                    {"What is the concept of community development in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"The process of improving community conditions through collective action", true}}},
                    {"How does the NSTP program promote health awareness?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Through health education and service projects", true}, {"By providing medicine", false}, {"By building hospitals", false}, {"By organizing sports", false}}},
                    {"What is the importance of project sustainability in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"To ensure long-term impact of community initiatives", true}}},
                    {"What is the role of community organizing in NSTP?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To mobilize community members for collective action", true}, {"To create events", false}, {"To design posters", false}, {"To take photos", false}}},
                    {"What is the concept of social justice in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"Promoting fair treatment and equal opportunities for all", true}}},
                    {"How does the NSTP program address educational needs?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Through literacy programs and educational support", true}, {"By building schools", false}, {"By providing scholarships", false}, {"By organizing classes", false}}},
                    {"What is the importance of community assessment in NSTP?", "SHORT_ANSWER", null,
                        new Object[][]{{"To identify community needs and resources for project planning", true}}},
                    {"What is the role of project implementation in NSTP?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To carry out planned community service activities", true}, {"To create reports", false}, {"To design posters", false}, {"To take photos", false}}}
                });
            addQuizWithQuestions(conn, "NSTP 2 - Hard", "UGRD-NSTP6102 National Service Training Program 2", "HARD", 30,
                new Object[][]{
                    {"Develop a sample community project plan that could be implemented under CWTS.", "SHORT_ANSWER", null,
                        new Object[][]{{"A plan could include a community needs assessment, objectives (e.g., environmental cleanup), activities, timeline, and budget.", true}}},
                    {"Which component of NSTP is designed to provide military training?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"ROTC", true}, {"CWTS", false}, {"LTS", false}, {"All of the above", false}}}
                });

            // UGRD-PHYED6102 Rhythmic Activities
            addQuizWithQuestions(conn, "Rhythmic Activities - Easy", "UGRD-PHYED6102 Rhythmic Activities", "EASY", 10,
                new Object[][]{
                    {"Which of the following is a rhythmic activity?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Folk Dance", true}, {"Weightlifting", false}, {"Chess", false}}},
                    {"Rhythmic activities often involve music.", "TRUE_FALSE", null,
                        new Object[][]{{"True", true}, {"False", false}}},
                    {"'Tempo' refers to the speed of the music.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"Which of the following is NOT a fundamental rhythm?", "MULTIPLE_CHOICE", null, new Object[][]{{"Sprinting", true}, {"Walking", false}, {"Running", false}, {"Hopping", false}}},
                    {"What is a 'choreography'?", "SHORT_ANSWER", null, new Object[][]{{"The sequence of steps and movements in a dance", true}}},
                    {"Folk dances are traditional dances of a country or region.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}},
                    {"What element of dance refers to the area the performers occupy?", "SHORT_ANSWER", null, new Object[][]{{"Space", true}}},
                    {"Ballroom dancing is typically performed by individuals alone.", "TRUE_FALSE", null, new Object[][]{{"False", true}, {"True", false}}},
                    {"What is 'syncopation' in music and dance?", "SHORT_ANSWER", null, new Object[][]{{"A disturbance or interruption of the regular flow of rhythm", true}}},
                    {"Flexibility is an important physical component for rhythmic activities.", "TRUE_FALSE", null, new Object[][]{{"True", true}, {"False", false}}}
                });
            addQuizWithQuestions(conn, "Rhythmic Activities - Medium", "UGRD-PHYED6102 Rhythmic Activities", "MEDIUM", 20,
                new Object[][]{
                    {"What is the relationship between rhythm and movement?", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythm provides the timing and structure for coordinated movement", true}}},
                    {"How does music influence dance performance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It provides timing, mood, and structure for the dance", true}, {"It only provides background noise", false}, {"It's not important for dance", false}, {"It only affects the audience", false}}},
                    {"What is the importance of proper posture in rhythmic activities?", "SHORT_ANSWER", null,
                        new Object[][]{{"It ensures proper alignment, prevents injury, and improves performance", true}}},
                    {"What is the difference between tempo and rhythm?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Tempo is the speed of the music, while rhythm is the pattern of beats", true}, {"They are exactly the same", false}, {"Tempo is for dance, rhythm is for music", false}, {"Tempo is for beginners, rhythm is for experts", false}}},
                    {"What is the role of breathing in rhythmic activities?", "SHORT_ANSWER", null,
                        new Object[][]{{"To maintain energy, control movement, and enhance performance", true}}},
                    {"How does choreography enhance dance performance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"By organizing movements into a structured sequence", true}, {"By making the dance longer", false}, {"By adding more dancers", false}, {"By changing the music", false}}},
                    {"What is the importance of warm-up exercises?", "SHORT_ANSWER", null,
                        new Object[][]{{"To prepare the body for activity and prevent injury", true}}},
                    {"What is the difference between folk dance and modern dance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Folk dance is traditional and cultural, while modern dance is contemporary and experimental", true}, {"They are exactly the same", false}, {"Folk dance is faster", false}, {"Modern dance is easier", false}}},
                    {"What is the role of space in dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To define the area of movement and create visual interest", true}}},
                    {"How does music affect emotional expression in dance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It influences the mood and intensity of movement", true}, {"It only affects the audience", false}, {"It's not important", false}, {"It only affects beginners", false}}},
                    {"What is the importance of timing in rhythmic activities?", "SHORT_ANSWER", null,
                        new Object[][]{{"To synchronize movement with music and other performers", true}}},
                    {"What is the difference between solo and group performance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Solo focuses on individual expression, while group emphasizes coordination", true}, {"They are exactly the same", false}, {"Solo is easier", false}, {"Group is more fun", false}}},
                    {"What is the role of energy in dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To create dynamic movement and express emotion", true}}},
                    {"How does costume affect dance performance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It enhances visual appeal and supports movement", true}, {"It only looks pretty", false}, {"It's not important", false}, {"It only affects the audience", false}}},
                    {"What is the importance of practice in rhythmic activities?", "SHORT_ANSWER", null,
                        new Object[][]{{"To improve skills, build muscle memory, and enhance performance", true}}},
                    {"What is the difference between technique and style?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Technique is the foundation of movement, while style is personal expression", true}, {"They are exactly the same", false}, {"Technique is for beginners", false}, {"Style is more important", false}}},
                    {"What is the role of music in cultural dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To preserve cultural traditions and enhance cultural expression", true}}},
                    {"How does rhythm affect coordination?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It provides timing for coordinated movement", true}, {"It only affects music", false}, {"It's not important", false}, {"It only affects beginners", false}}},
                    {"What is the importance of flexibility in dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To achieve proper form and prevent injury", true}}},
                    {"What is the difference between choreography and improvisation?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Choreography is planned movement, while improvisation is spontaneous", true}, {"They are exactly the same", false}, {"Choreography is easier", false}, {"Improvisation is better", false}}},
                    {"What is the role of expression in dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To communicate emotion and meaning through movement", true}}},
                    {"How does music structure affect dance composition?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It provides framework for movement patterns and transitions", true}, {"It only affects the audience", false}, {"It's not important", false}, {"It only affects beginners", false}}},
                    {"What is the importance of balance in rhythmic activities?", "SHORT_ANSWER", null,
                        new Object[][]{{"To maintain control and execute movements properly", true}}},
                    {"What is the difference between traditional and contemporary dance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Traditional follows established patterns, while contemporary is innovative", true}, {"They are exactly the same", false}, {"Traditional is easier", false}, {"Contemporary is better", false}}},
                    {"What is the role of music in dance education?", "SHORT_ANSWER", null,
                        new Object[][]{{"To develop rhythm, timing, and musicality in movement", true}}},
                    {"How does performance space affect dance?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It influences movement patterns and audience perception", true}, {"It only affects the audience", false}, {"It's not important", false}, {"It only affects beginners", false}}},
                    {"What is the importance of musicality in dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To interpret and express music through movement", true}}},
                    {"What is the difference between dance and exercise?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Dance is artistic expression, while exercise is physical fitness", true}, {"They are exactly the same", false}, {"Dance is easier", false}, {"Exercise is better", false}}},
                    {"What is the role of tradition in folk dance?", "SHORT_ANSWER", null,
                        new Object[][]{{"To preserve cultural heritage and pass on cultural values", true}}},
                    {"How does rhythm affect physical fitness?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"It enhances coordination and cardiovascular benefits", true}, {"It only affects music", false}, {"It's not important", false}, {"It only affects beginners", false}}}
                });
            addQuizWithQuestions(conn, "Rhythmic Activities - Hard", "UGRD-PE6102 Rhythmic Activities", "HARD", 30,
                new Object[][]{
                    {"Analyze the relationship between rhythm and human movement patterns.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythm provides a temporal framework that organizes and enhances human movement patterns, improving coordination and efficiency.", true}}},
                    {"Which element is NOT a fundamental component of rhythmic movement?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Random motion", true}, {"Tempo", false}, {"Beat", false}, {"Flow", false}}},
                    {"Explain the concept of rhythmic synchronization in group activities.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic synchronization involves coordinating movements with others, creating a sense of unity and enhancing group cohesion.", true}}},
                    {"What is the difference between rhythm and tempo?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythm is the pattern of movement, while tempo is the speed of the movement", true}, {"They are exactly the same", false}, {"Tempo is more important", false}, {"Rhythm is faster", false}}},
                    {"Analyze the impact of rhythmic activities on cognitive development.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic activities enhance cognitive development through improved timing, sequencing, and pattern recognition abilities.", true}}},
                    {"What is the role of music in rhythmic activities?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To provide a structured framework for movement and enhance emotional expression", true}, {"To distract from movement", false}, {"To replace movement", false}, {"To slow down movement", false}}},
                    {"Explain the concept of rhythmic complexity in dance.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic complexity involves layering different rhythmic patterns and movements to create sophisticated choreography.", true}}},
                    {"What is the difference between rhythmic activities and aerobic exercise?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythmic activities focus on movement patterns and expression, while aerobic exercise focuses on cardiovascular fitness", true}, {"They are exactly the same", false}, {"Aerobic exercise is more important", false}, {"They are unrelated", false}}},
                    {"Analyze the relationship between rhythm and cultural expression.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythm serves as a fundamental element in cultural expression, reflecting traditions, values, and social structures.", true}}},
                    {"What is the role of space in rhythmic activities?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To provide a context for movement and enhance spatial awareness", true}, {"To limit movement", false}, {"To replace time", false}, {"To eliminate rhythm", false}}},
                    {"Explain the concept of rhythmic improvisation.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic improvisation involves creating spontaneous movement patterns while maintaining rhythmic structure and musicality.", true}}},
                    {"What is the difference between rhythm and beat?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythm is the overall pattern, while beat is the regular pulse that underlies the rhythm", true}, {"They are exactly the same", false}, {"Beat is more important", false}, {"Rhythm is faster", false}}},
                    {"Analyze the impact of rhythmic activities on social development.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic activities promote social development through cooperation, communication, and shared experiences.", true}}},
                    {"What is the role of energy in rhythmic movement?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To give life and expression to movement patterns", true}, {"To exhaust participants", false}, {"To replace rhythm", false}, {"To eliminate movement", false}}},
                    {"Explain the concept of rhythmic flow in dance.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic flow involves smooth transitions between movements while maintaining rhythmic integrity and musicality.", true}}},
                    {"What is the difference between rhythmic activities and sports?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythmic activities focus on artistic expression, while sports focus on competition and physical performance", true}, {"They are exactly the same", false}, {"Sports are more important", false}, {"They are unrelated", false}}},
                    {"Analyze the relationship between rhythm and emotional expression.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythm provides a framework for emotional expression through movement, allowing for communication of feelings and moods.", true}}},
                    {"What is the role of timing in rhythmic activities?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To coordinate movement with musical elements and other participants", true}, {"To rush movements", false}, {"To replace rhythm", false}, {"To eliminate flow", false}}},
                    {"Explain the concept of rhythmic patterns in traditional dance.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic patterns in traditional dance reflect cultural heritage and social structures through specific movement sequences.", true}}},
                    {"What is the difference between rhythm and melody?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythm is the timing of movement, while melody is the sequence of musical notes", true}, {"They are exactly the same", false}, {"Melody is more important", false}, {"Rhythm is higher", false}}},
                    {"Analyze the impact of rhythmic activities on physical coordination.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic activities enhance physical coordination through practice of timing, sequencing, and spatial awareness.", true}}},
                    {"What is the role of dynamics in rhythmic movement?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To add variety and expression to movement through changes in energy and intensity", true}, {"To make movement harder", false}, {"To replace rhythm", false}, {"To eliminate flow", false}}},
                    {"Explain the concept of rhythmic structure in choreography.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic structure in choreography organizes movement patterns to create coherent and meaningful dance compositions.", true}}},
                    {"What is the difference between rhythmic activities and martial arts?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythmic activities focus on artistic expression, while martial arts focus on combat and self-defense", true}, {"They are exactly the same", false}, {"Martial arts are more important", false}, {"They are unrelated", false}}},
                    {"Analyze the relationship between rhythm and cultural identity.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythm serves as a marker of cultural identity, reflecting unique movement patterns and musical traditions.", true}}},
                    {"What is the role of repetition in rhythmic activities?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To reinforce movement patterns and create rhythmic structure", true}, {"To make activities boring", false}, {"To replace creativity", false}, {"To eliminate variety", false}}},
                    {"Explain the concept of rhythmic variation in movement.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic variation involves modifying movement patterns while maintaining rhythmic integrity and musicality.", true}}},
                    {"What is the difference between rhythm and harmony?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Rhythm is the timing of movement, while harmony is the combination of musical elements", true}, {"They are exactly the same", false}, {"Harmony is more important", false}, {"Rhythm is higher", false}}},
                    {"Analyze the impact of rhythmic activities on creative expression.", "SHORT_ANSWER", null,
                        new Object[][]{{"Rhythmic activities enhance creative expression through exploration of movement patterns and musical interpretation.", true}}}
                });

            // Data Structures - Hard 2
            addQuizWithQuestions(conn, "Data Structures - Hard 2", "UGRD-ITE6102 Data Structures", "HARD", 30,
                new Object[][]{
                    {"Explain the concept of cache-oblivious algorithms.", "SHORT_ANSWER", null,
                        new Object[][]{{"Cache-oblivious algorithms are designed to perform well on any memory hierarchy without knowing specific cache parameters.", true}}},
                    {"What is the difference between a segment tree and a binary indexed tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Segment trees support range queries and updates, while binary indexed trees only support prefix queries", true}, {"They are exactly the same", false}, {"Segment trees are faster", false}, {"Binary indexed trees are simpler", false}}},
                    {"Analyze the implementation of a lock-free data structure.", "SHORT_ANSWER", null,
                        new Object[][]{{"Lock-free data structures use atomic operations and memory ordering to ensure progress without traditional locks.", true}}},
                    {"What is the purpose of a van Emde Boas tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To support O(log log u) operations on integers from 0 to u-1", true}, {"To sort data", false}, {"To compress data", false}, {"To encrypt data", false}}},
                    {"Explain the concept of external memory algorithms.", "SHORT_ANSWER", null,
                        new Object[][]{{"External memory algorithms are designed to minimize I/O operations when data doesn't fit in main memory.", true}}},
                    {"What is the difference between a k-d tree and a quadtree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"K-d trees split space alternately by dimension, while quadtrees split into four equal parts", true}, {"They are exactly the same", false}, {"K-d trees are faster", false}, {"Quadtrees are simpler", false}}},
                    {"Analyze the implementation of a concurrent skip list.", "SHORT_ANSWER", null,
                        new Object[][]{{"Concurrent skip lists use lock-free techniques to allow concurrent access while maintaining the skip list structure.", true}}},
                    {"What is the role of a cuckoo hash table?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To provide O(1) worst-case lookup time with multiple hash functions", true}, {"To store data", false}, {"To sort data", false}, {"To compress data", false}}},
                    {"Explain the concept of succinct data structures.", "SHORT_ANSWER", null,
                        new Object[][]{{"Succinct data structures use space close to the information-theoretic minimum while supporting efficient operations.", true}}},
                    {"What is the difference between a rope and a string?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Ropes are balanced binary trees of strings, allowing efficient concatenation and splitting", true}, {"They are exactly the same", false}, {"Strings are faster", false}, {"Ropes are simpler", false}}},
                    {"Analyze the implementation of a concurrent binary heap.", "SHORT_ANSWER", null,
                        new Object[][]{{"Concurrent binary heaps use techniques like fine-grained locking or lock-free operations to allow concurrent access.", true}}},
                    {"What is the purpose of a fusion tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To support O(log n / log w) operations on w-bit integers", true}, {"To merge trees", false}, {"To split trees", false}, {"To balance trees", false}}},
                    {"Explain the concept of cache-friendly data structures.", "SHORT_ANSWER", null,
                        new Object[][]{{"Cache-friendly data structures are designed to minimize cache misses by considering memory layout and access patterns.", true}}},
                    {"What is the difference between a treap and a red-black tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Treaps use random priorities to maintain balance, while red-black trees use color properties", true}, {"They are exactly the same", false}, {"Treaps are faster", false}, {"Red-black trees are simpler", false}}},
                    {"Analyze the implementation of a concurrent B-tree.", "SHORT_ANSWER", null,
                        new Object[][]{{"Concurrent B-trees use techniques like lock coupling and optimistic concurrency control to allow concurrent access.", true}}},
                    {"What is the role of a count-min sketch?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To estimate frequency of elements in a data stream with limited memory", true}, {"To count elements", false}, {"To sort elements", false}, {"To compress data", false}}},
                    {"Explain the concept of dynamic optimality in binary search trees.", "SHORT_ANSWER", null,
                        new Object[][]{{"Dynamic optimality is the property of a binary search tree that performs as well as the best offline algorithm for any access sequence.", true}}},
                    {"What is the difference between a splay tree and an AVL tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"Splay trees move accessed nodes to root, while AVL trees maintain strict balance", true}, {"They are exactly the same", false}, {"Splay trees are faster", false}, {"AVL trees are simpler", false}}},
                    {"Analyze the implementation of a concurrent hash set.", "SHORT_ANSWER", null,
                        new Object[][]{{"Concurrent hash sets use techniques like lock striping and atomic operations to allow concurrent access while maintaining set properties.", true}}},
                    {"What is the purpose of a wavelet tree?", "MULTIPLE_CHOICE", null,
                        new Object[][]{{"To support efficient range queries on sequences", true}, {"To compress trees", false}, {"To merge trees", false}, {"To balance trees", false}}}
                });

            System.out.println("New sample data with subjects populated successfully.");

        } catch (SQLException e) {
            System.err.println("Error during sample data population: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addQuizWithQuestions(Connection conn, String title, String subject, String difficulty, int timeLimit, Object[][] questionsData) throws SQLException {
        String insertQuizSql = "INSERT INTO quizzes (title, subject, difficulty_level, time_limit_minutes) VALUES (?, ?, ?, ?)";
        String insertQuestionSql = "INSERT INTO questions (quiz_id, question_text, question_type, image_path) VALUES (?, ?, ?, ?)";
        String insertAnswerOptionSql = "INSERT INTO answer_options (question_id, option_text, is_correct) VALUES (?, ?, ?)";

        try (PreparedStatement pstmtQuiz = conn.prepareStatement(insertQuizSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmtQuiz.setString(1, title);
            pstmtQuiz.setString(2, subject);
            pstmtQuiz.setString(3, difficulty);
            pstmtQuiz.setInt(4, timeLimit);
            pstmtQuiz.executeUpdate();

            int quizId;
            try (ResultSet generatedKeys = pstmtQuiz.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    quizId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating quiz failed, no ID obtained.");
                }
            }

            for (Object[] qData : questionsData) {
                String qText = (String) qData[0];
                String qType = (String) qData[1];
                String qImagePath = (String) qData[2];
                Object[][] ansOptsData = (Object[][]) qData[3];

                try (PreparedStatement pstmtQuestion = conn.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtQuestion.setInt(1, quizId);
                    pstmtQuestion.setString(2, qText);
                    pstmtQuestion.setString(3, qType);
                    pstmtQuestion.setString(4, qImagePath);
                    pstmtQuestion.executeUpdate();

                    int questionId;
                    try (ResultSet qGeneratedKeys = pstmtQuestion.getGeneratedKeys()) {
                        if (qGeneratedKeys.next()) {
                            questionId = qGeneratedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating question failed, no ID obtained.");
                        }
                    }

                    for (Object[] ansOptData : ansOptsData) {
                        String optText = (String) ansOptData[0];
                        boolean isCorrect = (Boolean) ansOptData[1];
                        try (PreparedStatement pstmtAnsOpt = conn.prepareStatement(insertAnswerOptionSql)) {
                            pstmtAnsOpt.setInt(1, questionId);
                            pstmtAnsOpt.setString(2, optText);
                            pstmtAnsOpt.setBoolean(3, isCorrect);
                            pstmtAnsOpt.executeUpdate();
                        }
                    }
                }
            }
        }
    }
} 