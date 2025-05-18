# Spring Boot Blog Platform Backend
    ___.   .__                         .__          __    _____                     
    \_ |__ |  |   ____   ____   ______ |  | _____ _/  |__/ ____\___________  _____  
    | __ \|  |  /  _ \ / ___\  \____ \|  | \__  \\   __\   __\/  _ \_  __ \/     \
    | \_\ \  |_(  <_> ) /_/  > |  |_> >  |__/ __ \|  |  |  | (  <_> )  | \/  Y Y  \
    |___  /____/\____/\___  /  |   __/|____(____  /__|  |__|  \____/|__|  |__|_|  /
    \/           /_____/   |__|             \/                              \/

        :: Spring Boot ::                                                   (v3.2.5)

Welcome to the documentation for the backend of our Spring Boot Blog Platform! This Markdown file will provide an overview of the backend architecture, features, and usage instructions.

## Tech stack

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Java](https://img.shields.io/badge/spring%20boot-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) 
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens) 
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300000f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Hibernate Jpa](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![RESTfulAPI](https://img.shields.io/badge/RESTful%20API-black?style=for-the-badge)
![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)
![gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=Gradle&logoColor=white)

## Architecture Overview

The backend of our Spring Boot Blog Platform is built using the Spring Boot framework, integrating various libraries and components to provide a robust and feature-rich platform. Here's an overview of the key components:

- **Controllers**: Handle incoming HTTP requests and route them to the appropriate service methods.
- **Services**: Contain the business logic of the application, including user management, post management, category management, comment management, and email-sending functionality.
- **Repositories**: Interface with the database using Spring Data JPA to perform CRUD (Create, Read, Update, Delete) operations on entities such as users, posts, categories, and comments.
- **Security**: Implements security features such as authentication and authorization using Spring Security with JWT (JSON Web Tokens) support.
- **Exception Handling**: Provides global exception handling to handle errors and exceptions gracefully.
- **API Documentation**: Utilizes Swagger for generating interactive API documentation.
- **Database**: Utilizes MySQL as the relational database to store user data, blog posts, categories, comments, and other relevant information.

## Features

Our backend provides a comprehensive set of features to support the functionality of the blog platform:

- **JWT Authentication and Authorization**: Secure authentication and authorization mechanism using JSON Web Tokens.
- **User Management**: Allows administrators to create, update, delete, and manage user accounts, including roles and permissions.
- **Post Management**: Enables users to create, edit, delete, and manage blog posts, including categories and comments.
- **Category Management**: Supports creating, updating, deleting, and managing post categories to organize blog content.
- **Comment Management**: Allows users to add, edit, delete, and manage comments on blog posts.
- **Password Reset**: Provides functionality for users to reset their passwords via email.
- **Exception Handling**: Handles exceptions and errors gracefully with custom exception handlers.
- **API Documentation with Swagger**: Generates interactive API documentation using Swagger UI for easy reference and testing.
- **Mail Sender**: Integrates with Spring Mail to send emails for features such as password reset, and password update with MailTrap.
- **Spring Security**: Implements secure authentication and authorization using Spring Security.
- **Spring Data JPA**: Utilizes Spring Data JPA for simplified data access and manipulation.

## Usage

To use the backend of our Spring Boot Blog Platform, follow these steps:

1. **Setup**: Clone the repository and configure the database settings in the `application.properties` file.
2. **Build**: Build the project using Maven or your preferred build tool.
3. **Run**: Start the backend application using the generated JAR file or by running the application class.
4. **Test**: Use tools like Postman or curl to test the backend API endpoints.
5. **Explore API Documentation**: Access the interactive API documentation using Swagger UI to explore and test the available endpoints.
6. **Integrate**: Integrate the backend with the frontend of your blog platform to provide a complete user experience.



