# HAW-EML 15-Puzzle-CNN-Solver
<div style="display:flex; justify-content:center;">
  <img src="https://github.com/itakurah/HAW-EML-15-Puzzle-CNN-Solver/blob/main/app1.jpg" alt="Image" style="width:30%; height:auto;">
  <img src="https://github.com/itakurah/HAW-EML-15-Puzzle-CNN-Solver/blob/main/app2.jpg" alt="Image" style="width:30%; height:auto;">
</div>


This software project is implemented as an Android application written in Java and is focused on solving the 15-Puzzle game by first detecting the puzzle grid on an image using OpenCV. The digits within the grid are then extracted and classified using a convolutional neural network (CNN) that is pre-trained on the [MNIST](http://yann.lecun.com/exdb/mnist/) dataset. Once the digits have been classified, the puzzle is then solved using an informed search algorithm called IDA*. The use of OpenCV allows for efficient image processing and digit extraction while the CNN provides accurate digit classification. The IDA* algorithm, along with the linear-conflict heuristic, is used to determine the cost of each move and guide the search for the optimal solution.
## Usage
1. Select an image from your gallery or camera app
2. Wait for the grid to be detected
3. Modify incorrectly recognized digits
4. Solve the puzzle

Example image:
<div style="display:flex; justify-content:center;">
  <img src="https://github.com/itakurah/HAW-EML-15-Puzzle-CNN-Solver/blob/main/puzzle.jpg" alt="Image" style="width:40%; height:auto;">
</div>

## Build
When building the application you need to create a file called `local.properties` in the root folder containing the path of the installed SDK:
e.g.: `C:\\Users\\USERNAME\\AppData\\Local\\Android\\sdk`
## About
This project, created by Niklas Hoefflin, is a submission for the Embedded Machine Learning module at the Hamburg University of Applied Sciences under the supervision of Prof. Dr. Stephan Pareigis. It is shared on GitHub for educational and reference purposes only and can be used for commercial or any other non-academic purposes without the author's permission.
## License
This project is licensed under the MIT License. Please see the [LICENSE.md](https://github.com/itakurah/HAW-EML-15-Puzzle-CNN-Solver/blob/main/LICENSE) file for details.

