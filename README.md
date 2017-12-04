# Aztec-Digest

The Aztec-Digest repository contains code for the PDF upload service. It is a web-platform that enables users to upload publications (in PDF format or provide a PubMed Central ID) to the Aztec database. The service will extract relevant metadata from the publication and store the metadata in the database.

## Technologies
- Java (Version 1.8)
- Spring Boot
- Thyme Leaf
- Maven (All required dependencies are found in the pom.xml)

## Build
- Use Maven to get dependencies
- This project uses Spring Boot to create the web service

## Files
- /pdfs: Folder where PDFs are uploaded. (The PDFs are automatically deleted after processed)
- /src: All source code
  - /main: Code for web platform
    - /java
      - attributes: code used to extract different attributes from the text
      - web: code for creating web platform
    - /resources
      - /templates: html pages for web platform
      - application.properties: The configurations for Spring Boot
      - data.json: A list of all tools and metadata in Aztec database (this file will need to be updated often to include newly added tools)
      - docs.txt: A list of descriptions of each tool separated by newline. Note: The description of each tool must be stripped of all newlines.
      - model.zip: The binary file for the Doc2Vec model. Generated using the trainD2V function in DupDetector.
      - smtp.properties: The configurations needed for email service to work.
  - /test: Code for testing
    - /java: All test code
    - /resources
      - /pdfs: 100 PDFs from Bioinformatics that can be used for testing
      - dup_test.json: A JSON object of a tool used to test the correctness of the Duplicate Detection functionality.
      - test.json: A list of the 100 tools with their extracted metadata. This file can be used to compare accuracy of the extracted metadata fields.
- pom.xml: Contains configuration information (including dependencies) needed by Maven to build the project.
