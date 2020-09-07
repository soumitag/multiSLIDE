# Multi-omics Systems-Level Interactive Data Exploration (multiSLIDE)  
## User-driven concurrent heatmap visualization of large-scale multi-omics data  

## Availability

**multiSLIDE** is available online at: http://137.132.97.109:56695/multislide/#/. Alternatively, you may launch **multiSLIDE** locally on your computer following the instructions below.

## Launch multiSLIDE locally using a Docker Image  

The preferred way to use a local instance of **multiSLIDE** is using the pre-built Docker image available at Docker Hub.

To launch **multiSLIDE** on your local computer:

*	Install Docker for Mac/Windows/Linux following the instructions [here](https://docs.docker.com/get-docker/)
*	Pull the **multiSLIDE** Docker image using the following command in Terminal(Mac) or Command Prompt(Windows) or the Linux Shell

	```bash
	$ docker pull soumitag/multislide:2.0
	```
*	To launch **multiSLIDE** execute the following command:

	```bash
	$ docker run -d -p 8080:8080 -v multislide_home:/usr/local/multiSLIDE soumitag/multislide:2.0
	```

**multiSLIDE** should now be available at https://localhost:8080/multislide. Navigate to this link with your browser and start using **multiSLIDE**.

*	To stop **multiSLIDE**, first identify the name of the Docker container where **multiSLIDE** is running using the following command:
	
	```bash
	$ docker ps
	```
	This command produces an output that looks as follows:
	
	
	The last column of the output shows the container name. In this example it is 
	
	To stop this container use
	```bash
	$ docker stop 
	```

## Overview

**multiSLIDE** is an open-source tool for query-driven visualization of quantitative single- or multi-omics data. Using pathways and networks as the basis for data linkage, multiSLIDE provides an interactive platform for querying the multi-omics data by genes, pathways, and intermolecular relationships.  



![Visualization Workflow](multiSLIDE_Visualization_Workflow.png)



## Help

* How to use **multiSLIDE** ?
	* Videos tutorials demonstrating **multiSLIDE**'s functionalities are available [here](https://www.youtube.com/watch?v=AurU37gGxUI&list=PLh0_FmePh5yGFUpJZ9oYycdz8mgpxRdu1&index=1) 


[![HitCount](http://hits.dwyl.io/soumitag/multiSLIDE.svg)](http://hits.dwyl.io/soumitag/multiSLIDE)

