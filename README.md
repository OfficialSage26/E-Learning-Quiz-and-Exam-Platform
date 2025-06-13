# E-Learning Quiz and Exam Platform

A comprehensive JavaFX-based e-learning platform designed for educational institutions to conduct online quizzes and exams with advanced features for student assessment and progress tracking.

## Features

### 1. User Management
- Secure user registration and authentication
- Password hashing for enhanced security
- User profile management
- Session handling

### 2. Quiz System
- Multiple question types:
  - Multiple Choice
  - True/False
  - Short Answer
- Randomized questions and answers
- Timed exams with auto-submission
- Question navigation and review system
- Mark for review functionality
- Progress tracking during quiz

### 3. Difficulty Levels
- Three difficulty levels: Easy, Medium, Hard
- Progressive unlocking system:
  - Medium: Unlock after scoring ≥50% in Easy
  - Hard: Unlock after scoring ≥75% in Medium
- Subject-specific difficulty tracking

### 4. Performance Tracking
- Detailed quiz history
- Score tracking per attempt
- Time taken analysis
- Performance trends
- Subject-wise performance breakdown

### 5. Leaderboards
- Global rankings
- Subject-specific leaderboards
- Difficulty-based rankings
- Time-based performance tracking

### 6. Security Features
- Secure password storage
- SQL injection prevention
- Session management
- Prevention of multiple submissions
- Auto-submission on time expiry

### 7. User Interface
- Modern, responsive design
- Intuitive navigation
- Real-time feedback
- Progress indicators
- Color-coded results

## Technical Stack

- **Frontend**: JavaFX
- **Backend**: Java
- **Database**: SQLite
- **Build Tool**: Maven

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── school/
│   │           └── elearning/
│   │               ├── controllers/    # UI controllers
│   │               ├── models/         # Data models
│   │               ├── services/       # Business logic
│   │               └── database/       # Database management
│   └── resources/
│       └── fxml/                      # UI layouts
```

## Setup and Installation

1. **Prerequisites**
   - Java JDK 11 or higher
   - Maven
   - JavaFX SDK

2. **Database Setup**
   - The application uses SQLite
   - Database is automatically initialized on first run
   - Tables are created with proper schema

3. **Building the Project**
   ```bash
   mvn clean install
   ```

4. **Running the Application**
   ```bash
   mvn javafx:run
   ```

## Usage Guide

1. **Registration and Login**
   - Create a new account
   - Use credentials to log in
   - Access dashboard

2. **Taking Quizzes**
   - Select subject and difficulty
   - Answer questions within time limit
   - Review answers before submission
   - Get immediate feedback

3. **Progress Tracking**
   - View quiz history
   - Check performance metrics
   - Compare with leaderboards
   - Track improvement over time

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaFX team for the UI framework
- SQLite for the database system
- All contributors and testers

## Support

For support, please open an issue in the repository or contact the development team. 