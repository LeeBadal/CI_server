# DD2480-Assignment #2: CI Server
### Group 3, Spring 2021

CI Server is created for the assignment 'Continuous Integration' in the course DD2480 at KTH. The Program runs a server which has a listener for webhooks. Webhooks from GitHub's commits are considered valid and is further processed. In the process the project is built, tested and evaluated to later notify the user via GitHub statuses. Build and test results are stored in an external database for a history overview.

Travis build status:
[![Build Status](https://www.travis-ci.com/LeeBadal/CI_server.svg?token=7cmhVzehZexnVyrntj3T&branch=main)](https://www.travis-ci.com/LeeBadal/CI_server)

CI_server build list:
[http://www.expr.link/img/expr-link.png](http://expr.link/builds/list/all)

### How to run your CI_server
- git clone/download this repo
- First you need to create a [PAT](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token), this is so that your commit status can be updated. NOTE: The status updates will be set by the PAT's user (make sure you have check the boxes for repo authorization).
- Save the generated PAT in a file called token.txt in the root folder of the project.
- Using an IDE you can right click on the file ContinousIntegrationServer.java and run this file the server will run locally. The CI_server runs by default on port 8080.
- To allow GitHub to interact with the server you will have to either configurate your network settings or use a tunneling service such as [Ngrok](https://ngrok.com/).


You can then make POST requests and or visit the IP-adress to view all the builds created globally.

### Connecting CI_server to your GitHub repo
By visiting your repo and going to Settings -> Webhooks you can enter the CI-servers public IP.
**That is it!**

The implementation is dynamic and works for any public repository where the user of the PAT is a contributor.

### Project structure
 - **.idea**  Is Intellij specific folder, you do not need this folder if you are using a different IDEA
 - **src** source folder, contains all classes and main.
 - **tests** contains all tests for the classes and methods in **src** folder
 - **CI_server.iml** Is intellij auto-generated file, you do not need this file if you are using a different IDEA-
 - **.gitignore** development specific to ignore compiled binaries.
### [Requirements](#requirements)

To run the program you need the following software:

* Java 11
* Maven

To actually get the right results by the server, your commited project needs on one of the following automated build tools:
* Maven

### Running tests
To run the tests you need the software under [requirements](#requirements). And knowledge of how to run a java program in a IDE.

1. Download/clone repo
2. Make sure you have all the requirements and a working java IDE.
3. Go to the test folder run tests accordingly to your java editor

## List of contributions
All contributors have been part of the process such as code comments, system design and are satisfied with the eachothers contributions.

###### Lee Badal 
- Team Leader
- http methods
- insertDB method
- expr.link maintainer

###### Daniel Grunler


###### Matilda Rosenlew
- Pairprogrammed with Andreas Henriksson
- Clone method
- Build and Test methods
- Cleanup

###### Andreas Henriksson

 
###### Olle Hovmark

  
  
  
