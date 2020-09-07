# Launch multiSLIDE executing a Docker Image  

A Docker image for **multiSLIDE**, including installations and dependencies is available at Docker Hub

To easily launch **multiSLIDE** on your local computer:

*	Install Docker for Mac/Windows/Linux [Installation instructions](https://docs.docker.com/get-docker/)
*	Pull **multiSLIDE** Docker image using Terminal/Windows Command Prompt/Linux Shell

	```bash
	$ docker pull soumitag/multislide:2.0
	```
*	To launch **multiSLIDE** at https://localhost:8080/multislide

	```bash
	docker run -d -p 8080:8080 -v multislide_home:/usr/local/multiSLIDE soumitag/multislide:2.0
	```

Open your browser at https://localhost:8080/multislide and start using **multiSLIDE**









