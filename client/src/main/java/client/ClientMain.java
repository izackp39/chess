package client;

import ui.Repl;

public class ClientMain {
    public static void main(String[] args) {
        ServerFacade server = new ServerFacade(8080);
        new Repl(new ChessClient(server)).run();
    }
}
