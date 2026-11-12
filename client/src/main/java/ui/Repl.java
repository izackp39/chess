package ui;

import client.ChessClient;
import client.State;

import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_GREEN;
import static ui.EscapeSequences.WHITE_KING;

public class Repl {
    private final ChessClient client;

    public Repl(ChessClient client) {
        this.client = client;
    }

    public void run() {
        System.out.println(WHITE_KING + " Welcome to 240 chess. Type help to get started. " + WHITE_KING);
        Scanner scanner = new Scanner(System.in);
        while (client.state() != State.QUIT) {
            printPrompt();
            if (!scanner.hasNextLine()) {
                break;
            }
            String result = evalSafely(scanner.nextLine());
            System.out.println(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR);
        }
    }

    private String evalSafely(String line) {
        try {
            return client.eval(line);
        } catch (RuntimeException ex) {
            return "Something went wrong. Please try again.";
        }
    }

    private void printPrompt() {
        String label = (client.state() == State.SIGNEDIN) ? "[LOGGED_IN]" : "[LOGGED_OUT]";
        System.out.print("\n" + RESET_TEXT_COLOR + label + " >>> " + SET_TEXT_COLOR_GREEN);
    }
}
