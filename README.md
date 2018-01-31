# CSUILProgramTester (CUPT)
While at my first CS UIL contest, my group and I failed to correctly implement what we thought to be the most simple of the 12 
assigned problems. After spending valuable time diagnosing the problem, we realised that, in fact, one of the print statements had
two letters flipped. With this fact in mind, I built this small application for testing suites of UIL problems. 

## Downloads
To begin, downloads are below: 
  - [mac](https://github.com/zudsniper/CSUILProgramTester/tree/mac1.2.1)
  - [mac (bundled JRE)](https://github.com/zudsniper/CSUILProgramTester/tree/mac1.2.1b)
  - [windows](https://github.com/zudsniper/CSUILProgramTester/tree/win1.2.1)
  - [jar](https://github.com/zudsniper/CSUILProgramTester/tree/jar1.2.1)
  
_if you're having trouble with the mac or windows download, the .jar is platform independent, so long as java is installed._

## Contact
If you're experiencing issues, or have feature suggestions, please feel free to contact me via email. 

**jason [at-sign] holstr [dot] cc**

## Shortcuts
As of now, CUPT supports 3 shortcuts. 
  - ```CTRL``` + ```R```: Recompile and run current active class 
  - ```SHIFT``` + ```R```: Reload the registered classes (if a new class is added to the working directory and you'd like it recognise, reload.)
  - ```CTRL``` + ```SHIFT``` + ```R```: Recompile and run all loaded classes.

## Input & Output Specification
CUPT uses a fairly fault tolerant algorithm to find inputs and outputs for classes. First, the program will check if you've specified
explicit input and/or output files. To do so, one only needs to add the following to their class:

**input** 

```//%i:<filepath>.dat```

**output**

```//%o:<filepath>.log```

if explicit input/output isn't specified, the program will check for references to input within the class file. (_note: this 
currently only supports ```new File("path.dat")``` references to input, sorry_) If found, it then will check for an output file
by the same name as the input, but with the extension ```.log```.

and finally, if none of the above finds the appropriate input & output, class name is used with the appropriate extensions. 
(```.dat``` for input and ```.log``` for output)


## Screenshots
![image of class viewer screen.](https://i.imgur.com/d9JQWCP.png)

![second image of class viewer screen.](https://i.imgur.com/x8xpu8r.png)
