# Introduction
Recovery example shows a way how to use the Artezio Guaranteed Delivery library. The example emulates an high loaded billing adaptor. You can make payment requests for billing clients, edit recovery parameters of the requests and monitor the adapter gateway activity. The recovery example console allows manipulating with example data and performing tests online.

# Quickstart
Complete the steps described in the rest of this page to start the Recovery example.

# Prerequisites
To run this quickstart, you need the following prerequisites:

- Java 1.8 or greater.
- Maven 3.0.5 or greater.
- GIT 2.22.0 or greater.

# Step 1. Download sources
`git clone https://vcs.artezio.com/git/ART-GDL`

# Step 2. Compile sources
Set the project main directory and run:
`mvn clean install`

# Step 3. Run with Maven
Set subdirectory **example-billling-adaptor** and run:

`mvn spring-boot:run`

# Step 4. View the example console
Use your browser and open <http://localhost:8088/>

# Step 5. Start example
When you are opened the example first time some example data is generated. And you already can start the example with that data preset. Just press the button **START**
