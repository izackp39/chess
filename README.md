# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Server Design (Phase 2)

[Server design sequence diagram](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5MaCGEMQasRFQMABpjAC5ySipRGCpgZC0fVRgoFABHAFc1MGT2DhgwAAsoCAB3ZIAlNWwINAMAUQAPFWwCWoAKVXkwDNVgmABbNVVgexQAShhamHZLGEoSqBcAHTRhfxhC-Sm0QxRmlFakaa3OblgiuRSUHqg6gq3mLrlemFKkIpgAbwAiIfVRigfhEfvUoIsIi4oT8AL4rNAASTQ838c3QHBqSBCMxk2iUKnUMAAZsAkAgMqkYDoUCAIP8YFUbLUGgcjh0AKxOJz9UHg6ARdocNQgKDENrIiBE+Z8qDjH7jFymewlDLYADEABYAMwADgATG4QDA1ch7IUwDpyYYqvYkF0oHJjmhMIFzDAALQAPki1EoEQA2gAFADyZAAKgBdGAAel6lFWvzjd2AQ2BPxgPx5uHUpWgHDTGZ5KAGpIQBdhmHWlA93rYZz93x+SbQKaBYXTmYz2dUuag+fbhYzxdL5ZhmDrXGrXp90RUEVStq6lHaNrtPigVUy2XGmB0qWAAGsYM3W-1u72OP1oPMS2SYHbBnbVFj7JWojF3dOq1AIoyanUUCaFpxXaDUuR3b8PWnV0MAiMCnATP5hkBYEMzBCEqWAfJUi3LoK3RN8RBUGtcXkfFhgiMYwAAVQMKB2hPIYd1kMjlGGEjwhgAAxLF8loygWOAKk5kYsRBPIwlP29dZRAifioEEqZYDQDIEAQXd9yPeTFIfLxmBUtTCNnQwpJnBs-2ZQDWRAsCtQgqIp2g5BzDgpwtUQ-4RjGVDeQw+J9w4OZ5APdB8M4IyP29cS2PUCIRRQOQUHkhi6ME5i8Ri5Jp04hQOD41LtAi4jp2iglVDi-cfAUDIinaYAasKNLMFK9jsp0CJcvyaqikEoqTOnCdzl-FBF3XRlVKwQbHOkhyf0bUT0xBQcfnqoowwgEK0EWjMMzHSDTJgsAIj1LlEIWmAls7FaGvWzbto7McCMwZUIFVNVoHYMZjS4uB6mkOAFBgAAZCBbWdQ6SO-AMQ3DKNowMdQnTOuiW1TAcrvPPNRyMqdazyIb5pR1tyyzfQeyx9Gxym2Av3fOcYAQUGsXaEGwc3LIuh3PcEqPUSlJgTG+3vZIBifF8+qgmbfTmiyAKAw4bPAnGaacoIjpgeCPOQ7yQXQ-lMOw9IObAMKOAlkqMrKyibmS0T0tYsqOPa7jeJgbTtGE48iaGZrLda2miJQOSCvkfmDPU7nDzdkOhN01SECvWBBfyDgIDUGA9MGOQQEKc2A-8YamTl6ynVApwAEZ7OlyX4Gc2CNYrrWAR1tCZQiDIWwa6AkAALxQfNHvCmTiqiv3YpgeLEu6wo6oapqWsk70crymBp964f+rx+s5tZrFxoQSb8emsy5sTb2gQunartWwpbtCy+HuVmvDuO060DPyhUYvy6eRvu+tofrtMw4VnoqnVMqFA6BjSmnNBAqBrNXpYAhvnBs-ppD1CBvUMM9Q4YI2fLUVYN9u490dLUCIf8NroCfgNI+c0KGbXHLQyGdMg4M1Bog2ea1KFoCrsZEiC9yowCotPTht9uH20UJlJ2EQeKcFXnPD2Og5j0KoQImuG8OoKNDtecOGkebyJ6h7OOhkN7qNmoXf8LJgKlzApXahqsXIN3Lk3LybZW4YQ7kQ0UfcB7ALNqYi2DsKJGBQNwKqDVRH-wkRJLKS9nZVAGH4Qwa9CoBJodvCIVAIBIDNtTMx0sIhfD2rNZ+dd1YnQQu-QeZtQGvXVKkfIapsBYiPEDNcMAADirYNDIKlgXGA-oOlYLhvYVshCu7eNIWgchN1uHUK3pOOhsyGF5PzrJBma4un-EieI82o8gnj2ERElRPDfYHNiUYZ2siupaKEkomYyzVFjyyms+mKTtHKXjnoqO7zY7JF0Wkvp5lqiWXlmyNAZc7H7QcfXWxLiUK6zbseTuRRiH91Nns0ikirYbK6FstQ7RolSLajI128QECdNbL1NRpkNG4uYJKSlQx14sJrtTCIbS8XdP3ofbe+T+m-FGf8VC-pBWtgRNIK6pRCgfCSufK6lpQAHnkl-K6QqUAADliYwgjMU6uB0ymv0qWK4VAYTUoAlVKmVPgVXEx5IqkAyr5U8nVVq1MOrqlKjAWqDgAB2NwTgUBOGNPULUcBfoADZ4CVUMPil0ZTmEFIGTDSMMZ1XjNRZM8UMyuGbUQq64mlN5msFoTmsRm1+jmrdW2R+qy+nrMnj4fF7Q4Axvxezbc3yjwFqGMLR8iMHB5yBTLEFxdrEdHgrw4+L8G7GqQs3Nxvl9Z6ENrhE2nqAn7OxcEo5tUTlEsdiSl2cjfmexOWc7dklXmsNPTor5kcjynuMepQFJ8LGgpLhOiuU6VbehnXC9+87XE+T1nNTxEze7oo3aywJl7BGNpQM2ntEwL0xOkavFe+LFLvE+LsUoQjxXSBmHIvD8BCjDDjTS69lFCNDpLRk6NCUm2th5YwvltLzGNnVZay+5c9Rag1HqjYBq1ZGvzYR7afGBOetqW9SwoTGb4ZNEgM0YB5NqTKDAAAUtk5E+LjQOoPPGtWib+lBmoqm6M6aUWFGIVMst-9ELYAQMAeTUA4AQEZlANMAB1FgCJsHRgAEJAwUHAAA0mqiTEQpOCeLeyh5ub0CVp+M51zlAPNeZJhmbjkqYv8bi3Wmc6yABWunm06axO2o2naH0EaGBKvtosB32ETgLFzbnMvXl0t4GAfmAv1H5iFsL4W6NQwZKOqxCsbFK2hX+w1s74UtyXXNFd1w10Ys3VimJ1swAiP3ah4lcTSUntuWex5zoqP1reWdu9hk6tPv+V81943ZZTfBZCn9pTRNOKW4u0D7cbNot8QRLbAjdtIcIwe1qx3j35Cw4ouYuXDuHuo0ylAik7sR00ujnST2TEwZQSOou73FZOC+yJxxmtAOeQRe45dWF1vGwxXVnCxt+4T089efyCVAqbG2EJUSY3ONvasuOiFtkKcwvVrZP7IGkU86wkFQ8oVoOB34c89u2BEiIdbO0dV2GPiFC9p-Vs95kRpc61z2UKOYeXOGml4iCPQ5EmvMj19CWsk5LY4s-lDYin2Pmz9ipCYZMvTesqVz0CVOwIdHMRACVYDAGwM5wgUzjNuiJwGdBmDsFw2MPF0tGYfg+-OKZht3A8CEsxeDielf6LQ8XvbibiSaAzDUtHAS2h+i-P6HkXHqTCcLIJl73JTCONJoD3N2uwe34B4IkAA) covering all seven endpoints: register, login, logout, list games, create game, join game, and clear.

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
