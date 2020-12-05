# Backend for COMmunication
## Description
This repository contains the API for Furhat skill (we call it COMmunication). In this project, we used the following technologies:

- Docker and Docker-compose
- Percona as the DB
- FastAPI

## Setup
### Prerequisites
For running this project, there is a need to install docker and docker compose from the following links:

- https://docs.docker.com/engine/install/
- https://docs.docker.com/compose/install/

### Installation
In order to setup the project, just clone the project. If you have installed git cli, follow the instructions below:


    git clone https://github.com/AS2-P2-Group-11/COMmunicationBackend.git
    bash build-env.sh
### Start the agent
To start the agent just enter the command below:

    bash start-agent.sh
Important note: For the first time when you run build-env.sh command, there is no need to start the project.

### Stop the agent
To stop the agent just enter the command below:

    bash stop-agent.sh
    
## Access the documentation
To access the documentation, after installation and seting up, open either one of:

- http://127.0.0.1:9000/docs
- http://127.0.0.1:9000/redoc

Using the API is straight forward by reading documentation
    
