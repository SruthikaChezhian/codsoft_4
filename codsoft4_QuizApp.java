import java.util.Scanner;

class QuizQuestion {
    private String question;
    public String[] options;
    private int correctOption;

    public QuizQuestion(String question, String[] options, int correctOption) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
    }

    public boolean isCorrect(int userChoice) {
        return userChoice == correctOption;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return options[correctOption];
    }
}

class QuestionTimer extends Thread {
    private final int questionNumber;
    private final long timeLimitMillis;
    private final Object lock;
    private boolean answered = false;
    private boolean timeUp = false;

    public QuestionTimer(int questionNumber, long timeLimitMillis, Object lock) {
        this.questionNumber = questionNumber;
        this.timeLimitMillis = timeLimitMillis;
        this.lock = lock;
    }

    @Override
    public void run() {
        synchronized (lock) {
            try {
                lock.wait(timeLimitMillis);
                if (!answered) {
                    timeUp = true;
                    System.out.println("\nTime's up! You did not respond to the question no. " + questionNumber + " within the allotted time. \nYou may try again, but the attempt will not affect your score.");
                }
            } catch (InterruptedException e) {
                // Timer interrupted (user answered in time)
            }
        }
    }

    public void markAnswered() {
        answered = true;
        synchronized (lock) {
            lock.notify(); // Notify the timer to stop waiting
        }
        interrupt(); // Stop the timer
    }

    public boolean isTimeUp() {
        return timeUp;
    }
}

public class codsoft4_QuizApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Object lock = new Object();

        // Create quiz questions
        QuizQuestion[] questions = {
            new QuizQuestion("What is the hardest natural substance on Earth?",
                    new String[]{"Gold", "Iron", "Diamond", "Granite"}, 2),
            new QuizQuestion("What is the capital of Japan?",
                    new String[]{"Beijing", "Seoul", "Bangkok", "Tokyo"}, 3),
            new QuizQuestion("Who developed the theory of relativity?",
                    new String[]{"Isaac Newton", "Galileo Galilei", "Nikola Tesla", "Albert Einstein"}, 3),
            new QuizQuestion("What is the largest organ in the human body?",
                    new String[]{"Heart", "Liver", "Skin", "Brain"}, 2),
            new QuizQuestion("Which country has won the most FIFA World Cups?",
                    new String[]{"Germany", "Italy", "Brazil", "Argentina"}, 2)
        };

        int totalQuestions = questions.length;
        int score = 0;

        System.out.println("Welcome to the Quiz App!");
        System.out.println("Answer the following questions:");

        for (int i = 0; i < totalQuestions; i++) {
            System.out.println("\nQuestion " + (i + 1) + ": " + questions[i].getQuestion());
            for (int j = 0; j < questions[i].options.length; j++) {
                System.out.println((j + 1) + ". " + questions[i].options[j]);
            }

            QuestionTimer timer = new QuestionTimer(i + 1, 15000, lock); // 15 seconds
            timer.start();

            int userChoice = -1;
            boolean validInput = false;
            while (!validInput && !timer.isTimeUp()) {
                System.out.print("Enter your choice (1-" + questions[i].options.length + "): ");
                if (scanner.hasNextInt()) {
                    userChoice = scanner.nextInt();
                    if (userChoice >= 1 && userChoice <= questions[i].options.length) {
                        validInput = true;
                        timer.markAnswered(); // Mark the question as answered
                    } else {
                        System.out.println("Invalid choice. Please select 1-" + questions[i].options.length);
                        scanner.nextLine(); // Clear the invalid input
                    }
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next(); // Clear the invalid input
                }
            }

            if (timer.isTimeUp()) {
                System.out.println("Nice try. The correct answer was: " + questions[i].getCorrectAnswer());
            } else if (validInput) {
                if (questions[i].isCorrect(userChoice - 1)) {
                    System.out.println("Correct!");
                    score++;
                } else {
                    System.out.println("Incorrect. The correct answer was: " + questions[i].getCorrectAnswer());
                }
            }
        }

        System.out.println("\nQuiz completed!");
        System.out.println("Your final score: " + score + "/" + totalQuestions);

        scanner.close();
    }
}
