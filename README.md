# Swiss Tournament Manager

This is a console program that allows you to easily manage a Swiss-style chess tournament through simple commands.

## Features

Add, remove, enable, and disqualify players.
Start and manage tournament rounds.
Record game outcomes and view standings.
Use various tiebreakers and configure priority order.
Add players between rounds (for flexible management).
Track player Elo based on match outcomes.

## Commands

add player: name : Adds a player to the tournament.

remove player: name : (Only before starting) Removes a player.

enable player: name : Enables a previously disabled player.

disqualify player: name : Disqualifies a player (not paired).

start : Starts the tournament.

current round : Displays current round pairings.

score : Shows current tournament standings.

game: white - black => outcome : Records game outcome (1-0, 0-1, 1/2-1/2, suspended).

redo : Redoes the last round (useful for changes or adding players).

help : Displays this help message.

tiebreakers: <tiebreaker1, tiebreaker2, ..., tiebreakerN> : Sets tiebreakers and order of priority.

iterations: <iterations> : Set iterations for recursive performance tiebreaker.

config: <configPath> : Set path to config file for tiebreakers preferences (json format).

example : Runs an example tournament.

### Additional Notes

Enter commands one at a time and press Enter.
See [this](https://en.wikipedia.org/wiki/Swiss-system_tournament) for more on Swiss-style tournaments.

## Pairing System

Uses a priority queue based on points and black piece games played.
Higher priority players get white, then next best opponent gets black.
Players without an opponent get a point (doesn't count as a game).

## Elo

Player Elo changes based on match outcomes and is saved in a binary file.
New players and completed tournaments create/save the database.

## Tiebreak Systems

Multiple supported systems with configurable priority order.
Options include Black Games, Buchholz, Elo, Linear Performance, Progressive, FIDE Performance, and Sonneborn-Berger.
Specify priority with tiebreakers command or a JSON configuration file.

## Prerequisites
To use the app, you need Java 21 and Maven 3.9.6. If you don't have them installed, you can follow these steps:

### Installing Java:

Visit the official [OpenJDK JDK 21.0.2 download page](https://jdk.java.net/21/). 
Click the Download button for your operating system.

Once the download is complete unzip the file an put this jdk folder sowhere on your computer. 

You have to add to the [PATH variable](https://www.java.com/en/download/help/path.html) the location of the /bin folder inside the jdk folder.

Also you have to create the JAVA_HOME variable with the path to the jdk folder in an analogous form to the PATH variable.

To checkout if your instalation was correct execut this command

```console
java --version
```

You would see something like this

```console
openjdk 21.0.2 2024-01-16
OpenJDK Runtime Environment (build 21.0.2+13-58)
OpenJDK 64-Bit Server VM (build 21.0.2+13-58, mixed mode, sharing)
```

### Installing Maven:

Go to the [Apache Maven Downloads page](https://maven.apache.org/download.cgi).
Click the link for the Binary zip archive. Once the download is complete, extract the Maven folder.

Add the /bin directory inside the Maven folder to your PATH environment variable.

Create the MAVEN_HOME variable with the path to the Maven folder.

## Usage as console app

Clone this repository from a command window and run:
```console
mvn clean package
```
Wait for the project to compile. Then run:
```console
java -jar target/swiss-tournament-core-1.0.0-ALPHA-jar-with-dependencies.jar
```
You will see a brief usage guide similar to this README.

## Version

This is an alpha version (v1.0.0-ALPHA) with potential bugs and incomplete features.

## Contribution
Want to help? Here's how:

Bug reports: Submit them in the Issues section.

Improvements: Suggest them in Issues or fork & send a pull request.

Other ways: Translate, write docs, answer questions in Issues.

## Licence
[Apache-2.0 license](https://www.apache.org/licenses/LICENSE-2.0)


