# Multi-threaded Dictionary Server

## Description
This Java-based project implements a multi-threaded dictionary server allowing multiple clients to concurrently search meanings, add and remove words, and update meanings via a reliable TCP connection. It showcases the use of Java sockets and threads as mandated by the project specifications.

## Features
- **Query Word Meanings**: Search for meanings of specific words.
- **Add New Words**: Insert new words along with their meanings.
- **Remove Words**: Delete existing words from the dictionary.
- **Update Meanings**: Modify meanings of existing words.
- **Graphical User Interface**: Clients interact through a GUI.
- **Error Handling**: Handles errors like bad inputs, network issues, and I/O exceptions.

## Prerequisites
- Java JDK 11 or later
- Java Runtime Environment (JRE)

## Project Structure
src/ # Source files for the server and client
server/ # Server application code
client/ # Client application code
common/ # Utilities and constants shared between the server and client
lib/ # Libraries and dependencies
data/ # Dictionary data files
build/ # Compiled classes and JAR files
README.md # Project instructions and information


# Setup Instructions

### Compiling the Code
1. Navigate to the project root directory.
2. Compile the source code:
   ```bash
   javac -d build src/**/*.java
   ```
3. Create JAR files for the server and client
   ``` bash
   jar cfe build/DictionaryServer.jar server.Main -C build/ .
   jar cfe build/DictionaryClient.jar client.Main -C build/ .
   ```

# Author : Di Lu

This single file is ready to be used as your `README.md` in your project's root directory. It gives an overall view of your project, detailed instructions for compilation and execution, and lists all the necessary prerequisites and features. Adjust the placeholders and paths as needed to fit your project's specifics.
