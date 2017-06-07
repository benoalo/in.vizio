# in.vizio
in.visio is an n dimensionnal image viewer. It aims to provide essential visualisation tool in a simple GUI
steered toward non specialist users. In other words, it tries to get visualisation out of the way to better
support annotation and quantitative image analysis.

# Current developments

The projects is relying on VTK (for visualisation), ImgLib2 (for image data structure) and can be run as an
ImageJ plugin.

The project is still in its early developments but I believe already an interesting demo of what can be done 
with VTK and its java wrappers. The following figure illustrate the current status: on the left the rendering
of the Mitosis dataset (ImageJ sample dataset, 3D+time+channel). the channel are visualize simultianeously 
while the time can be browsed interactively with a slider. On the right the t1-head dataset (ImageJ sample 
dataset) showing both cropping and slicing (along random axis). Future development will involve refactoring 
of the user interaction, large data handling, geometry visualisation and better integration in ImageJ2 
ecosystem.


![in.vizio illustration](https://github.com/benoitlo/in.vizio/blob/master/invizio_illustration.PNG "in.vizio illustration")


# Installation

To give a try to the project, compile VTK 7.1 with java wrappers, clone the repo and import it in your IDE
as a Maven project for compilation. 

