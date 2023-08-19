# UP-SMF - Exam and admission

The Exam and Admissions Management System is a comprehensive module that handles the entire process of conducting exams, managing admissions, processing payments from institutes to the government, and providing students with their mark sheets. The system covers the complete workflow from announcing exam timetables to generating and distributing mark sheets.


## Table of Contents

- [Description](#description)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

## Description

Provide a short introduction to your project. What is its purpose, and what problem does it solve? Mention the main technologies or frameworks used.

## Features

List the key features or functionalities of your project.

## Installation

1. Setup the postgres 
   1. sudo docker run --name wingspan -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 127.0.0.1:5432:5432 -v /data:/var/lib/postgresql/data -d bitnami/postgresql:10.15.0-debian-10-r84
   2. sudo docker exec -it wingspan -h localhost -p 5432 -U postgres -d frac_tool; 

```bash
# Installation steps
$ git clone <git@github.com:UPHRH-platform/examsAndAdmissions.git>
$ cd project-directory
$ java ExamsAndAdmissionsApplication.java
