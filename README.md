# TrafficMonitoringSystem

TrafficMonitoringSystem is a Java application that utilizes OpenCV image processing library. It will analyze CCTV traffic cameras, generate reports and give insights to interested parties.

## Installation

You can use any Java IDE of your choice.
Eclipse was used on this project.

### Setup development tools
Download latest [Eclipse](https://www.eclipse.org/downloads/packages/).

Download [GIT](https://git-scm.com/download/win) for windows.

Download [Scene Builder](https://gluonhq.com/products/scene-builder/).

### Setup development environment

Clone the project from repository.
Launch Git Bash terminal on windows.

```bash
git clone https://github.com/kimjavier43/TrafficMonitoringSystem.git
```

Launch Eclipse IDE and open the project and select project folder.

```bash
File > Open Projects from File System
```

Reference external libraries build dependencies.

```bash
Right click project folder > Build Path > Configure Build Path > Libraries
```

From Libraries tab, Add External JARs.
Navigate to TrafficMonitoringSystem/third-party directory and select all jars.

Repeat the step and add OpenCV library.
TrafficMonitoringSystem/opencv/opencv-343.jar

Add OpenCV native libraries.
From Libraties tab, select opencv-343.jar and collapse select Native Library Location and edit.
Locate and select directory TrafficMonitoringSystem/opencv-343/build/java/x64 --for 64 bit OS

### Enable logging facility
Add slf4j config file to classpath
project folder > Build Path > Configure Build Path > Libraries > Add External Class Folder
TrafficMonitoringSystem/third-party/config

## Generate database data
curl "https://api.mockaroo.com/api/fe841400?count=1000&key=e2b92d20" > "TMS.sql"

Run project

