# Launch multiSLIDE using a Docker Image  

The preferred way to use a local instance of **multiSLIDE** is using the pre-built Docker image available at Docker Hub.

To launch **multiSLIDE** on your local computer:

*	Install Docker for Mac/Windows/Linux following the instructions [here](https://docs.docker.com/get-docker/)
*	Pull the **multiSLIDE** Docker image using the following command in Terminal(Mac) or Command Prompt(Windows) or the Linux Shell

	```bash
	$ docker pull soumitag/multislide:2.0
	```
*	To launch **multiSLIDE** execute the following command:

	```bash
	docker run -d -p 8080:8080 -v multislide_home:/usr/local/multiSLIDE soumitag/multislide:2.0
	```

**multiSLIDE** should now be available at https://localhost:8080/multislide. Navigate to this link with your browser and start using **multiSLIDE**.









