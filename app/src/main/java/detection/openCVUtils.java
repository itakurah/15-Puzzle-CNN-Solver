package detection;

import android.graphics.Bitmap;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.opencv.imgproc.Imgproc.*;

public class openCVUtils {

    private static String APP_PATH = null;
    private static String PIECES_PATH = null;
    private static Mat gridBitwise = new Mat();
    private static Mat rawBitwise = new Mat();
    private static Mat gridGray = new Mat();
    private static Mat rawGray = new Mat();
    private static Mat gridThreshold = new Mat();
    private static Mat rawThreshold = new Mat();
    private static Mat gridBlur = new Mat();
    private static Mat rawBlur = new Mat();
    private static Mat rawResized = new Mat();

    public static List<Mat> detectGrid(File image) {
        List<Mat> listMat = new ArrayList<>();
        APP_PATH = image.getParent() + "/";
        PIECES_PATH = image.getParent() + "/pieces/";
        Mat originalImage = loadImage(image.getAbsolutePath());
        Mat originalImageCopy = originalImage.clone();
        //convert image to black and white
        Mat resized = resizeImage(APP_PATH, originalImageCopy);
        rawResized = resized.clone();
        Mat resizedCopy = resized.clone();
        Mat resultBitwise = convertRawImageToBitwise(APP_PATH, resizedCopy);
        listMat.add(resultBitwise);
        listMat.add(resized);
        return listMat;
    }

    public static HashMap<Integer, List<Bitmap>> getTensorImages(List<Mat> listMat) {
        //warpe image
        Mat warped = openCVUtils.perspectiveTransform(APP_PATH, listMat.get(0), listMat.get(1));
        Mat resultGrid = convertWarpedGridToBitwise(APP_PATH, warped);
        //remove lines
        openCVUtils.removeLinesDynamic(APP_PATH, resultGrid);
        openCVUtils.removeLinesStatic(APP_PATH, resultGrid);
        //divide image
        List<Mat> pieces = openCVUtils.divideImage(PIECES_PATH, resultGrid);
        //find digits in pieces
        return openCVUtils.findDigits(APP_PATH, pieces);
    }

    public static Mat loadImage(String filePath) {
        return Imgcodecs.imread(filePath);
    }

    public static void saveImage(String filePath, String fileName, Mat image) {
        Imgcodecs.imwrite(filePath + fileName, image);
    }

    private static Mat convertWarpedGridToBitwise(String filePath, Mat puzzleImage) {
        Mat imageCopy = puzzleImage.clone();
        //convert to grey image
        Mat resultGray = new Mat(imageCopy.height(), imageCopy.width(), CvType.CV_8UC1);
//        imageCopy.convertTo(resultGray,CvType.CV_8UC1,1.5);
//        imageCopy.convertTo(resultGray, CvType.CV_8UC1, 1.0);//increase contrast
//        saveImage(filePath, "g_contrast.jpg", resultGray);
        Core.addWeighted(imageCopy, 1.5, imageCopy, 0.0, 0.0, imageCopy);
        saveImage(filePath, "g_contrast.jpg", imageCopy);
        Imgproc.cvtColor(imageCopy, resultGray, Imgproc.COLOR_RGB2GRAY);
        gridGray = resultGray.clone();
        //save grey image
        saveImage(filePath, "g_grey.jpg", resultGray);
        //copy Mat attribs
        Mat resultBlur = resultGray.clone();
        //apply gaussian blur to grey image
//        Imgproc.medianBlur(resultBlur,resultBlur,3);
        Imgproc.GaussianBlur(resultGray, resultBlur, new Size(7, 7), 0, 0, 0);
        gridBlur = resultBlur.clone();
        //save gaussian blur image
        saveImage(filePath, "g_blur.jpg", resultBlur);
        //copy Mat attribs
        Mat resultThreshold = resultBlur.clone();
        //apply adaptive threshold
        Imgproc.adaptiveThreshold(resultBlur, resultThreshold, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);//11 2
        gridThreshold = resultThreshold.clone();
        //save adaptive threshold
        saveImage(filePath, "g_threshold.jpg", resultThreshold);
        //copy Mat attribs
        Mat resultBitwise = resultThreshold.clone();
        //apply bitwise inversion
        Core.bitwise_not(resultThreshold, resultBitwise);
        //apply opening
//        Imgproc.morphologyEx(resultThreshold, resultThreshold, MORPH_CLOSE, kernel);
        Mat kernel = Imgproc.getStructuringElement(MORPH_RECT, new Size(3, 3));
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_ERODE, kernel);
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_ERODE, kernel);
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_OPEN, kernel);
        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_OPEN, kernel);
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_DILATE, kernel);
        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_DILATE, kernel);
        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_DILATE, kernel);
//        Imgproc.morphologyEx(resultBlur, resultBlur, MORPH_DILATE, kernel);
        gridBitwise = resultBitwise.clone();
        //save bitwise inversion
        saveImage(filePath, "g_bitwise.jpg", resultBitwise);
        //show detected rectangle and marks corners
        Mat bitwiseCopy = resultBitwise.clone();
        return resultBitwise;
    }

    /**
     * Preprocess image for corner detection
     *
     * @param filePath filePath path to save image
     * @param rawImage image to be processed
     * @return bitwise inverted image
     */
    private static Mat convertRawImageToBitwise(String filePath, Mat rawImage) {
        //clone orig image
        Mat imageCopy = rawImage.clone();
        //convert to grey image
        Mat resultGray = new Mat(imageCopy.height(), imageCopy.width(), CvType.CV_8UC1);
        Core.addWeighted(imageCopy, 1.0, imageCopy, 0.0, 0.0, imageCopy);
        saveImage(filePath, "r_contrast.jpg", imageCopy);
        Imgproc.cvtColor(imageCopy, resultGray, Imgproc.COLOR_RGB2GRAY);
        rawGray = resultGray.clone();
        //save grey image
        saveImage(filePath, "r_grey.jpg", resultGray);
        //copy Mat attribs
        Mat resultBlur = resultGray.clone();
        //apply gaussian blur to grey image
        Imgproc.GaussianBlur(resultGray, resultBlur, new Size(9, 9), 0, 0, 0);
        rawBlur = resultBlur.clone();
        //save gaussian blur image
        saveImage(filePath, "r_blur.jpg", resultBlur);
        //copy Mat attribs
        Mat resultThreshold = resultBlur.clone();
        //apply adaptive threshold
        Imgproc.adaptiveThreshold(resultBlur, resultThreshold, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 13, 5);
        rawThreshold = resultThreshold.clone();
        //save adaptive threshold
        saveImage(filePath, "r_threshold.jpg", resultThreshold);
        //copy Mat attribs
        Mat resultBitwise = resultThreshold.clone();
        //apply bitwise inversion
        Core.bitwise_not(resultThreshold, resultBitwise);
        Mat kernel = Imgproc.getStructuringElement(MORPH_RECT, new Size(5, 5));
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_OPEN, kernel);
        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_DILATE, kernel);
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_DILATE, kernel);
//        Imgproc.morphologyEx(resultBitwise, resultBitwise, MORPH_DILATE, kernel);
        drawRectangle(resultBitwise,new Scalar(0,0,0),10);
        rawBitwise = resultBitwise.clone();
        //save bitwise inversion
        saveImage(filePath, "r_bitwise.jpg", resultBitwise);
        //show detected rectangle and marks corners
        Mat bitwiseCopy = resultBitwise.clone();
        addGreenFrame(filePath, bitwiseCopy, rawImage);
        return resultBitwise;
    }


    /**
     * Draws a green frame around the found rectangle and marks the corners
     *
     * @param filePath       filePath path to save image
     * @param inversionImage bitwise inversion image
     * @param rawImage       image to draw on
     * @throws Exception when points array has null value
     */
    private static void addGreenFrame(String filePath, Mat inversionImage,
                                      Mat rawImage) {
        // Find contours of an image
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(
                inversionImage,
                contours, new Mat(
                        inversionImage.height(),
                        inversionImage.width(),
                        inversionImage.type()),
                Imgproc.RETR_EXTERNAL, // We are looking for external contours
                Imgproc.CHAIN_APPROX_SIMPLE);
        // Find index of the biggest contour
        int biggestContourIndex = openCVUtils.getBiggestPolygonIndex(contours);
        drawContours(rawImage, contours, biggestContourIndex, new Scalar(0, 255, 0), 3);
        saveImage(filePath, "r_marked.jpg", rawImage);
    }

    /**
     * Used to find index of the biggest polygonal curve.
     *
     * @param contours Contours for which index of the biggest polygonal curve is calculated.
     * @return Returns an integer representing index of biggest polygonal curve.
     */
    private static int getBiggestPolygonIndex(List<MatOfPoint> contours) {
        double maxValue = 0;
        int maxValueIndex = 0;
        for (int i = 0; i < contours.size(); i++) {
            double contourArea = Imgproc.contourArea(contours.get(i));
            // If current value (contourArea) is bigger than maxValue then it becomes maxValue
            if (maxValue < contourArea) {
                maxValue = contourArea;
                maxValueIndex = i;
            }
        }
        return maxValueIndex;
    }

    /**
     * Used to get corner points of provided polygonal curve.
     *
     * @param poly Polygonal curve for which corner points are found.
     * @return Returns an array of sorted found corner points.
     */
    private static Point[] getPoints(MatOfPoint poly) {
        MatOfPoint2f approxPolygon = openCVUtils.approximatePolygon(poly);
        Point[] sortedPoints = new Point[4];

        if (!approxPolygon.size().equals(new Size(1, 4))) {
            return sortedPoints;
        }

        // Calculate the center of mass of our contour image using moments
        Moments moment = Imgproc.moments(approxPolygon);
        int centerX = (int) (moment.get_m10() / moment.get_m00());
        int centerY = (int) (moment.get_m01() / moment.get_m00());

        // We need to sort corner points as there is no guarantee that we will always get them in same order
        for (int i = 0; i < approxPolygon.rows(); i++) {
            double[] data = approxPolygon.get(i, 0);
            double dataX = data[0];
            double dataY = data[1];

            // Sorting is done in reverence to center points (centerX, centerY)
            if (dataX < centerX && dataY < centerY) {
                sortedPoints[0] = new Point(dataX, dataY);
            } else if (dataX > centerX && dataY < centerY) {
                sortedPoints[1] = new Point(dataX, dataY);
            } else if (dataX < centerX && dataY > centerY) {
                sortedPoints[2] = new Point(dataX, dataY);
            } else if (dataX > centerX && dataY > centerY) {
                sortedPoints[3] = new Point(dataX, dataY);
            }
        }
        return sortedPoints;
    }

    /**
     * Approximates a polygonal curve.
     *
     * @param poly Polygonal curve.
     * @return destination
     */
    private static MatOfPoint2f approximatePolygon(MatOfPoint poly) {
        MatOfPoint2f destination = new MatOfPoint2f();
        MatOfPoint2f source = new MatOfPoint2f();
        poly.convertTo(source, CvType.CV_32FC2);

        // Approximates a polygonal curve with the specified precision
        Imgproc.approxPolyDP(
                source,
                destination,
                0.02 * Imgproc.arcLength(source, true),
                true
        );
        return destination;
    }

    /**
     * Removes the horizontal and vertical gridlines of given image
     *
     * @param filePath         file path of given image
     * @param transformedImage image where lines will be removed
     */
    private static void removeLinesDynamic(String filePath, Mat transformedImage) {
        //clone source image
        Mat horizontalImage = transformedImage.clone();
        //overwrite horizontal lines in grid
        overwriteLines(horizontalImage, transformedImage, new Scalar(0, 0, 0), new Size(120, 1));//black color
        Mat verticalImage = transformedImage.clone();
        //overwrite vertical lines in grid
        overwriteLines(verticalImage, transformedImage, new Scalar(0, 0, 0), new Size(1, 120));//black color
        //TESTING OVERWRITING OUTER BORDERS WITH BLACK
        Rect rect = new Rect(new Point(0, 0), new Point(transformedImage.height(), transformedImage.width()));
        Imgproc.rectangle(transformedImage, rect, new Scalar(0, 0, 0), 6);//255,255,255
        // Overwrite horizontal black lines with white lines in source image
        saveImage(filePath, "g_without_lines.jpg", transformedImage);
    }

    /**
     * overwrites lines in grid with given color in source image
     *
     * @param copyImage        copy of source image
     * @param transformedImage image where lines will be removed
     * @param color            color with which the lines will be overwritten
     * @param size             size of structuring element (40, 1) for horizontal lines, (1, 40) for vertical
     */
    private static void overwriteLines(Mat copyImage, Mat transformedImage, Scalar color, Size size) {
        //create kernel
        Mat kernel = Imgproc.getStructuringElement(MORPH_RECT, size);
        //apply opening
        Imgproc.morphologyEx(copyImage, copyImage, MORPH_OPEN, kernel);
        Imgproc.morphologyEx(copyImage, copyImage, MORPH_OPEN, kernel);
//        Imgproc.morphologyEx(copyImage, copyImage, MORPH_DILATE, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(copyImage, contours, new Mat(), Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        // Overwrite black lines with white lines in source image
        Imgproc.drawContours(transformedImage, contours, -1, color, 4);
    }

    private static void removeLinesStatic(String filePath, Mat transformedImage) {

//        Rect rect = new Rect(new Point(0, 0), new Point(image.height(), image.width()));
        drawRectangleAround(transformedImage, new Scalar(0, 0, 0), 25);
        saveImage(filePath, "g_without_lines.jpg", transformedImage);
    }

    private static void drawRectangleAround(Mat image, Scalar color, int thickness) {
        int x1 = 0;
        int y1 = 0;
        int x2 = image.width();
        int y2 = image.height() / 4;
        y1 = 0;
        //remove horizontal lines
        for (int i = 0; i < 4; i++) {
            Rect rect = new Rect(new Point(0, 0), new Point(x2, y2));
            Imgproc.rectangle(image, rect, color, thickness);//255,255,255 white 0,0,0 black
            y2 += image.height() / 4;
        }
        x1 = 0;
        y1 = 0;
        x2 = image.width() / 4;
        y2 = image.height();
        //remove vertical lines
        for (int j = 0; j < 4; j++) {
            Rect rect = new Rect(new Point(0, 0), new Point(x2, y2));
            Imgproc.rectangle(image, rect, color, thickness);//255,255,255 white 0,0,0 black
            x2 += image.width() / 4;
        }
//        Rect rect = new Rect(new Point(0, 0), new Point(image.height(), image.width()));
//        Imgproc.rectangle(image, rect, color, thickness);//255,255,255 white 0,0,0 black
    }

    private static void drawRectangle(Mat image, Scalar color, int thickness) {
        Rect rect = new Rect(new Point(0, 0), new Point(image.cols(), image.rows()));
        Imgproc.rectangle(image, rect, color, thickness);//255,255,255 white 0,0,0 black
    }


    /**
     * Resizes given image
     *
     * @param filePath filePath path to save image
     * @param rawImage image to resize
     * @return resized image scaled down by 50%
     */
    private static Mat resizeImage(String filePath, Mat rawImage) {
        Mat resize = new Mat();
        while ((rawImage.width() | rawImage.height()) <= 1000) {
            Imgproc.resize(rawImage, rawImage, new Size(), 1.2, 1.2, INTER_CUBIC);
        }
        while ((rawImage.width() & rawImage.height()) >= 1000 & ((rawImage.width() / 2 & rawImage.height() / 2) >= 1000)) {
            Imgproc.resize(rawImage, rawImage, new Size(), 0.8, 0.8, INTER_AREA);
        }
//        Imgproc.resize(rawImage, resize, new Size(), 0.5, 0.5, INTER_AREA);
        saveImage(filePath, "r_resized.jpg", rawImage);
        return rawImage;
    }

    /**
     * Transforms given image to bird eyes view
     *
     * @param filePath filePath path to save image
     * @param rawImage image to transform
     * @return transformed image
     */
    private static Mat perspectiveTransform(String filePath, Mat rawImage, Mat originalImage) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(
                rawImage,
                contours, new Mat(
                        rawImage.height(),
                        rawImage.width(),
                        rawImage.type()),
                Imgproc.RETR_EXTERNAL, // We are looking for external contours
                Imgproc.CHAIN_APPROX_SIMPLE);
        // Find index of the biggest contour
        int biggestContourIndex = openCVUtils.getBiggestPolygonIndex(contours);
        // Find corner points and mark them green
        Point[] points = new Point[0];
        points = openCVUtils.getPoints(contours.get(biggestContourIndex));
        //use calculated points of the biggest contour
        MatOfPoint2f sourceMatrix = new MatOfPoint2f(
                points[0],
                points[1],
                points[2],
                points[3]);
        //As the dest image is 500x500
        MatOfPoint2f destMatrix = new MatOfPoint2f(
                new Point(0, 0),
                new Point(1000 - 1, 0),
                new Point(0, 1000 - 1),
                new Point(1000 - 1, 1000 - 1)
        );
        //create warp matrix
        Mat warpMatrix = Imgproc.getPerspectiveTransform(sourceMatrix, destMatrix);
        Mat result_warped = new Mat();
        //warp image
        Imgproc.warpPerspective(originalImage, result_warped, warpMatrix, new Size(1000, 1000));
        //save warped image
        saveImage(filePath, "r_warped.jpg", result_warped);
        return result_warped;
    }

    /**
     * Divides the source image in 16 pieces
     *
     * @param filePath         filepath of source image
     * @param transformedImage image to be divided
     * @return list of 16 pieces
     */
    private static List<Mat> divideImage(String filePath, Mat transformedImage) {
        List<Mat> listOfPieces = new ArrayList<>();
        int x = transformedImage.cols() / 4;
        int y = transformedImage.rows() / 4;

        int count = 1;
        //get pieces sorted from left to right, top to bottom
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rect rectOfImage = new Rect(new Point(j * x, i * y), new Point((j + 1) * x, (i + 1) * y));
                Mat piece = transformedImage.submat(rectOfImage);

//                Imgproc.GaussianBlur(piece, piece, new Size(3, 3), 0, 0, 0);
//                Mat kernel = Imgproc.getStructuringElement(MORPH_RECT, new Size(2, 2));
//////                //apply opening
//                Imgproc.morphologyEx(piece, piece, MORPH_OPEN, kernel);//open
                //draw rectangle to improve boundig box prediction
                drawRectangle(piece, new Scalar(0, 0, 0), 40);
                listOfPieces.add(piece);
                saveImage(filePath, count + ".jpg", piece);
                count++;
            }
        }
        return listOfPieces;
    }

    /**
     * Finds digits using the biggest contour draws a bounding box around it and cuts out the digits
     *
     * @param filePath path of digits
     * @param pieces   list of pieces to be identified
     */
    private static HashMap<Integer, List<Bitmap>> findDigits(String filePath, List<Mat> pieces) {
        HashMap<Integer, List<Bitmap>> digitOrderMap = new HashMap<>();
        int countPieces = 1;
        Mat pieceCopy = new Mat();
        for (Mat piece : pieces
        ) {
            // Find contours of an image
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(piece, contours, new Mat(), RETR_EXTERNAL,
                    Imgproc.CHAIN_APPROX_SIMPLE);
            //convert from CV_8UC1 to CV_8UC3
            piece.convertTo(piece, CvType.CV_8UC3);
            Imgproc.cvtColor(piece, piece, COLOR_GRAY2RGB);
            pieceCopy = piece.clone();
            //draw green frame around all digits
            int countContour = 1;
            for (MatOfPoint point : contours
            ) {
                Rect boundRect = Imgproc.boundingRect(point);
                int x = boundRect.x;
                int y = boundRect.y;
                int width = boundRect.width;
                int height = boundRect.height;
                int area = width * height;
                int minArea = 200;//threshold for random pixel detection
                if (area > minArea) {
                    Mat currentDigit = extractDigitFromPiece(filePath + "digits/" + countPieces + "/", countContour, piece, boundRect);
                    Mat currentDigitCopy = currentDigit.clone();
                    Mat tensorImage = createTensorImage(filePath + "tensor_digits/" + countPieces + "/", countContour, currentDigitCopy);
                    Bitmap bitmap = convertMatToBitmap(tensorImage);
                    //create map of tensor images
                    if (digitOrderMap.containsKey(countPieces)) {
                        digitOrderMap.get(countPieces).add(bitmap);
                    } else {
                        digitOrderMap.put(countPieces, new ArrayList<>());
                        digitOrderMap.get(countPieces).add(bitmap);
                    }
                    Scalar color = new Scalar(0, 255, 0);
                    Imgproc.rectangle(pieceCopy, new Point(x, y), new Point(x + width, y + height), color, 1);
                }
                countContour++;
            }
            //save all pieces as image
            saveImage(filePath + "pieces_marked/", countPieces + "_digit_marked.jpg", pieceCopy);
            countPieces++;
        }
//        digitOrderMap.entrySet().forEach(entry -> {
//            System.out.println(entry.getKey() + " " + entry.getValue());
//
//        });
        System.out.println(digitOrderMap.values().stream().mapToInt(List::size).sum());
        return digitOrderMap;
    }

    /**
     * Extracts the bounding box of found digit
     *
     * @param filePath   path of pieces
     * @param count      count of current piece
     * @param pieceImage image from current piece
     * @param boundRect  bounding box from current digit
     * @return
     */
    private static Mat extractDigitFromPiece(String filePath, int count, Mat pieceImage, Rect boundRect) {
        Mat digit = pieceImage.submat(boundRect);
        while ((digit.width() | digit.height()) > 26) {
            Imgproc.resize(digit, digit, new Size(), 0.9, 0.9, INTER_AREA);
        }
        saveImage(filePath, "digit_" + count + ".jpg", digit);
        return digit;
    }

    /**
     * Calculates the missing pixels for each piece
     * and adds black border with specific size that
     * image satisfies given size of 28x28 for tensorflow lite
     *
     * @param filePath   path of images
     * @param count      used for file saving
     * @param digitImage image of digit
     * @return
     */
    private static Mat createTensorImage(String filePath, int count, Mat digitImage) {
        int h = digitImage.rows();
        int w = digitImage.cols();
        int top = (28 - h) / 2;//28-27=1
        int bottom = 28 - h - top;//28-27-1=0
        int left = (28 - w) / 2;//28-16=6
        int right = 28 - w - left;//28-16=12-6=6
        //add black border relative to digit
        Core.copyMakeBorder(digitImage, digitImage, top, bottom, left, right, Core.BORDER_CONSTANT, new Scalar(0, 0, 0));
        saveImage(filePath, "digit_" + count + "_merged.jpg", digitImage);
        return digitImage;
    }

    /**
     * Converts a bitmap object to a mat object using opencv's built-in method
     *
     * @param bitmap bitmap object
     * @return mat object
     */
    private static Mat convertBitmapToMat(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        org.opencv.android.Utils.bitmapToMat(bmp32, mat);
        return mat;
    }

    /**
     * Converts a mat object to a bitmap using opencv's built-in method
     *
     * @param mat mat object
     * @return bitmap object
     */
    private static Bitmap convertMatToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }


}
