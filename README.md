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

```add player: <name>```: Adds a player to the tournament.

```remove player: <name>```: (Only before starting) Removes a player.

```enable player: <name>```: Enables a previously disabled player.

```disqualify player: <name>```: Disqualifies a player (not paired).

```start```: Starts the tournament.

```current round```: Displays current round pairings.

```score```: Shows current tournament standings.

```game: <white> - <black> => <outcome>```: Records game outcome (1-0, 0-1, 1/2-1/2, suspended).

```redo```: Redoes the last round (useful for changes or adding players).

```help```: Displays a help message.

```tiebreakers: <tiebreaker1, tiebreaker2, ..., tiebreakerN> ```: Sets tiebreakers and order of priority.

```config: <configPath>```: Set path to config file for tiebreakers preferences (json format).

```example```: Runs an example tournament.

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
In order to properly set the tiebreakers you have to type the tibreakers names like it's shown in this example:

´´´console
tiebreakers: sonneborn berger, buchholz, progressive, black games, fide performance, linear performance, elo
´´´

You should see this respond by the app:

´´´console
Tiebreakers order:
  Sonneborn-Berger
  Buchholz
  Progressive
  Black Games
  FIDE Perf
  Linear Perf
  Elo
´´´

## Prerequisites
To use the app, you need Java 21 and Maven 3.9.6. If you don't have them installed, you can follow these steps:

### Installing Java:

Visit [Java 21 Semeru JVM download page](https://developer.ibm.com/languages/java/semeru-runtimes/downloads/). 
Select your operating system and download the JDK version.

Once the download is complete unzip the file an put this jdk folder sowhere on your computer. 

You have to add to the [PATH variable](https://www.java.com/en/download/help/path.html) the location of the /bin folder inside the jdk folder.

Also you have to create the JAVA_HOME variable with the path to the jdk folder in an analogous form to the PATH variable.

To checkout if your instalation was correct execut this command

```console
java -version
```

You would see something like this

```console
openjdk version "21.0.6" 2025-01-21 LTS
IBM Semeru Runtime Open Edition 21.0.6.0 (build 21.0.6+7-LTS)
Eclipse OpenJ9 VM 21.0.6.0 (build openj9-0.49.0, JRE 21 Windows 10 amd64-64-Bit Compressed References 20250121_374 (JIT enabled, AOT enabled)
OpenJ9   - 3c3d179854
OMR      - e49875871
JCL      - e01368f00df based on jdk-21.0.6+7)
```

### Installing Maven:

Go to the [Apache Maven Downloads page](https://maven.apache.org/download.cgi).
Click the link for the Binary zip archive. Once the download is complete, extract the Maven folder.

Add the /bin directory inside the Maven folder to your PATH environment variable.

Create the MAVEN_HOME variable with the path to the Maven folder.

## Usage as console app

Clone this repository from a command window and run:
```console
cd <your git repositories location>/swiss-tournament/swiss-tournament-core
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


