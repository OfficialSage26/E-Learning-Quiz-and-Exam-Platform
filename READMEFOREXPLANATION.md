Main Components:
Core Application (MainApp.java):
The entry point of the application
Initializes the database and loads the login screen
Sets up the main window and UI components

Controllers:
LoginController.java - Handles user authentication
DashboardController.java - Main dashboard after login
QuizSelectionController.java - Manages quiz selection
QuizInterfaceController.java - Handles the actual quiz-taking interface

Models (Data Classes):
User.java - User information
Quiz.java - Quiz structure
Question.java - Question details
AnswerOption.java - Answer choices
QuizAttempt.java - Quiz attempt records

Services:
AuthService.java - Authentication logic
QuizService.java - Quiz management
LeaderboardService.java - Leaderboard functionality

Database:
DatabaseManager.java - Handles database operations
Uses SQLite (elearning_quiz.db) for data storage

Key Features:
User registration and login
Quiz selection by subject and difficulty
Interactive quiz interface
Progress tracking
Leaderboard system
User guide

Technical Stack:
Java 11 or higher
JavaFX 17.0.6 for UI
SQLite for database
Maven for project management

User Flow:
User logs in through LoginController
After successful login, DashboardController shows the main interface
User can select quizzes through QuizSelectionController
Quiz taking is handled by QuizInterfaceController
Results and progress are tracked and displayed

The application follows a clean architecture with separation of concerns:
Controllers handle user interactions
Models define data structures
Services contain business logic
Database layer manages data persistence