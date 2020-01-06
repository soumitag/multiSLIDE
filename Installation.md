# multiSLIDE Installation Instructions  

To install **multiSLIDE** on your local computer, please follow the steps below to install the software prerequisites manually: 

## System Requirements

*	**multiSLIDE** requires a memory configuration of atleast 16 GB RAM to work well on large-scale datasets. 
*	Any standard web browser can be used for data visualization

### Windows 

* 


### macOS

* **Java Development Kit (JDK)**

	*	**multiSLIDE** was developed and tested on JDK 8. However other versions of JDK can also be installed. 
	*	JDK 8 is available [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (before downloading the file you have to accept the license agreement).

		For macOS, **jdk-8u231-macosx-x64.dmg** is the JDK installer. 
	*	Double-click the .dmg file and follow the instructions on screen to install the JDK.

* **GlassFish Server**

	*	**multiSLIDE** was developed and tested using GlassFish 4.1.1.
	*	GlassFish 4.1.1 is available [here](https://download.oracle.com/glassfish/4.1.1/release/index.html).
	*	Download **glassfish-4.1.1.zip**.
	*	Unzip and place the folder in your preferred installation directory.	

* **MongoDB**

	*	**mongo-community** can be installed with the package manager **Homebrew**.
	*	To install Homebrew issue the following command at the _Terminal_:

		<pre><code>/usr/bin/ruby -e "$(curl -fsSL 
			https://raw.githubusercontent.com/Homebrew/install/master/install)" </code></pre>
	*	**mongo-community** can be installed by issuing the following commands:

		<pre><code>brew tap mongodb/brew
		brew install mongodb-community
		brew services start mongodb-community </code></pre>
	*	**MongoDB** requires creating a specific directory that it uses as its repository. Create the database directory **/data/db** in **/System/Volumes/Data**
	*	Issue the following command to verify **mongod** is working correctly:
		<pre><code>sudo mongod --dbpath /System/Volumes/Data/data/db </code></pre> 

* **Python**
	
	*	Download Python 3.7 for macOS from [here](https://www.anaconda.com/distribution/#macos).
	*	Detailed installation instructions for Anaconda's implementation of Python are available [here](https://docs.anaconda.com/anaconda/install/mac-os/)
	*	**Numpy (Python Package)** _Anaconda’s implementation of Python has **Numpy** pre-installed, so no
additional configuration steps are required here. For other implementations
of Python Numpy may have to be installed separately._
	*	**Scipy (Python Package)**
		To install **Scipy** issue the following command at the _Terminal_:
		<pre><code>conda install –c anaconda scipy</code></pre>
	*	**fastcluster(Python Package)**
		To install **fastcluster 1.1.25** issue the following command at the _Terminal_:
		<pre><code>conda install -c conda-forge fastcluster</code></pre>






