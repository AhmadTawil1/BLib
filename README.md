# BLib Project

A sophisticated client-server library management system designed to optimize library operations. This project leverages JavaFX for the client interface and a robust server architecture for handling requests and database interactions.

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Overview

The BLib Project is a state-of-the-art library management system that streamlines the process of managing library resources. It is divided into three main components:

- **BLibClient**: A JavaFX-based client application that provides a user-friendly interface for managing library operations such as book borrowing, returns, and subscriber management.
- **BlibServer**: A server application that processes client requests, manages database operations, and ensures secure communication between clients and the server.
- **BlibCommon**: A shared codebase containing models and enums used by both the client and server to maintain consistency and reduce redundancy.

## Project Structure

- `PROJECT/G3_Assignment3-Project/`
  - `BLibClient/`: Client-side application
  - `BlibServer/`: Server-side application
  - `BlibCommon/`: Shared code

## Features

- **User Management**: Manage subscribers and their accounts.
- **Book Management**: Add, update, and remove books from the library.
- **Borrowing and Returns**: Track book borrowings and returns.
- **Reports**: Generate reports on library activities.
- **Reservations**: Allow subscribers to reserve books.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- JavaFX SDK
- A compatible IDE (e.g., Eclipse, IntelliJ IDEA)
- MySQL or another compatible database system

## Installation

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd BLib
   ```

2. **Set Up the Database**:
   - Ensure your database server is running.
   - Import the provided database schema.

3. **Configure the Server**:
   - Update the database connection settings in the server configuration file.

4. **Build the Project**:
   - Open the project in your IDE.
   - Build the project to resolve dependencies.

## Usage

1. **Start the Server**:
   - Run the `BlibServer` application.
   - Ensure the server is listening on the correct port.

2. **Start the Client**:
   - Run the `BLibClient` application.
   - Log in using the provided credentials.

## Troubleshooting

- **Server Not Starting**: Check if the database is running and the connection settings are correct.
- **Client Connection Issues**: Ensure the server is running and the client is configured to connect to the correct server address and port.

## SQL Tools and Methods

- **MySQL**: The primary database management system used for storing and retrieving data.
- **JDBC**: Java Database Connectivity is used to interact with the MySQL database from the Java application.
- **SQL Queries**: Custom SQL queries are used to perform CRUD operations on the database.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
