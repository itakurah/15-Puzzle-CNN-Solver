package detection;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;

public class ImageUtils {
    private static final int INPUT_TENSOR_1 = 28;
    private static final int INPUT_TENSOR_2 = 28;
    private static final int NUM_OF_CHANNELS = 4;
    private static final List<Integer> LABELS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
    private static Interpreter interpreter;


    /**
     * Converts bitmap to a byte buffer object
     *
     * @param bitmap
     * @return byteBuffer
     * @link source: <a href="https://github.com/ZZANZUPROJECT/TFLite-Object-Detection/blob/3b3bd19bae59ec4293bda6ec4bf11306b8e1ab5e/app/src/main/java/com/example/android/alarmapp/tflite/TensorFlowImageClassifier.java#L102">...</a>
     */
    private static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true); //convert to ARGB that each pixel is stored on 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(INPUT_TENSOR_1 * INPUT_TENSOR_2 * NUM_OF_CHANNELS);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_TENSOR_1 * INPUT_TENSOR_2];
        bmp.getPixels(intValues, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_TENSOR_1; ++i) {
            for (int j = 0; j < INPUT_TENSOR_2; ++j) {
                final int val = intValues[pixel++];
//                byteBuffer.putFloat(((val >> 16) & 0xFF)/255.f);
//                byteBuffer.putFloat(((val >> 8) & 0xFF)/255.f);
//                byteBuffer.putFloat(((val) & 0xFF)/255.f);
                byteBuffer.putFloat(((val >> 16) & 0xFF)/255.f+((val >> 8) & 0xFF)/255.f+((val) & 0xFF)/255.f);
            }
        }
        return byteBuffer;
    }

    /**
     * Returns the game state of current puzzle
     *
     * @param tensorImages
     * @return
     * @throws Exception
     */
    public static HashMap<String, List<String>> getGameState(HashMap<Integer, List<Bitmap>> tensorImages, Context context) {
        List<Integer> gameState = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0));
        HashMap<String, List<String>> predictionMap = new HashMap<>();
        try {
            gameState = getPosOfBlankTile(tensorImages, context);//get pos of blank tile in grid
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        predictionMap = getPredictionMap(tensorImages);//get map with predictions
//        System.out.println("DETECTED DIGITS FROM MODEL " + predictionMap.get("raw"));
//        System.out.println("PARSED DIGITS " + predictionMap.get("parsed"));
//        System.out.println("ZERO STATE " + gameState);
        predictionMap.put("idx", new ArrayList<>());
        predictionMap.get("idx").add(String.valueOf(gameState.indexOf(0)));
        return predictionMap;
    }

    /**
     * Returns a map of integers and bitmaps where Integer is the position of the bitmap extracted from the grid
     *
     * @param tensorImages
     * @return
     * @throws Exception
     */
    private static HashMap<String, List<String>> getPredictionMap(HashMap<Integer, List<Bitmap>> tensorImages) {
        List<String> digitPrediction = new ArrayList<>();
        List<String> rawDigitPrediction = new ArrayList<>();
        HashMap<String, List<String>> predictionMap = new HashMap<>();
        predictionMap.put("raw", new ArrayList<>());
        predictionMap.put("parsed", new ArrayList<>());
        for (Map.Entry<Integer, List<Bitmap>> entry : tensorImages.entrySet()
        ) {
            List<String> digits = new ArrayList<>();
            StringBuilder concat = new StringBuilder();
            for (Bitmap bitmap : entry.getValue()
            ) {
                int prediction = predict(bitmap);
                digits.add(String.valueOf(prediction));
                concat.append(prediction);
            }
            rawDigitPrediction.add(String.valueOf(concat));
            digitPrediction.add(parsePrediction(digits));//add parsed digit to list
        }
        predictionMap.get("raw").addAll(rawDigitPrediction);
        predictionMap.get("parsed").addAll(digitPrediction);
        return predictionMap;
    }

    /**
     * Returns the location of zero in grid
     *
     * @param tensorImages
     * @return
     * @throws Exception
     */
    private static List<Integer> getPosOfBlankTile(Map<Integer, List<Bitmap>> tensorImages, Context context) throws Exception {
        Integer[] gameState = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        List<Integer> listGameState = new ArrayList<>(Arrays.asList(gameState));
        int countTensorImages = 0;
        for (Map.Entry<Integer, List<Bitmap>> entry : tensorImages.entrySet()
        ) {
            countTensorImages += entry.getValue().size();
            if (entry.getValue().size() > 2) {
                Log.e("Error", "Folder has more than 2 tensor images");
                System.out.println("Folder has more than 2 tensor images");
                Toast.makeText(context, "Error while validating piece " + entry.getKey(), Toast.LENGTH_LONG).show();
            }
            listGameState.set((entry.getKey() - 1), -1);
        }
        if (countTensorImages != 21) {//if tensor images don't add up to 21 e.g. to many blank tiles or digit detection didn't work properly
            Log.e("Error", "Tensor image count is not 21");
            System.out.println("Tensor image count is not 21");
            Toast.makeText(context, "One or more digits are illegible, check the digits and redraw them.", Toast.LENGTH_LONG).show();
        }
        if(!listGameState.contains(0)){
            throw new Exception("Puzzle does not contain blank tile");
        }
        return listGameState;
    }

    /**
     * Parses the result from prediction to integer
     *
     * @param digits list of digits predicted by predict method
     * @return list of integers with results
     * @throws Exception thrown when digit is not in range of 1-15
     */
    private static String parsePrediction(List<String> digits) {
        String result = digits
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(""));
        switch (result) {
            case "1":
                return "1";
            case "2":
                return "2";
            case "3":
                return "3";
            case "4":
                return "4";
            case "5":
                return "5";
            case "6":
                return "6";
            case "7":
                return "7";
            case "8":
                return "8";
            case "9":
                return "9";
            case "00":
                return "10";
            case "10":
                return "10";
            case "01":
                return "10";
            case "07":
                return "10";
            case "70":
                return "10";
            case "11":
                return "11";
            case "77":
                return "11";
            case "71":
                return "11";
            case "17":
                return "11";
            case "12":
                return "12";
            case "21":
                return "12";
            case "27":
                return "12";
            case "72":
                return "12";
            case "02":
                return "12";
            case "20":
                return "12";
            case "13":
                return "13";
            case "31":
                return "13";
            case "37":
                return "13";
            case "73":
                return "13";
            case "03":
                return "13";
            case "30":
                return "13";
            case "14":
                return "14";
            case "41":
                return "14";
            case "47":
                return "14";
            case "74":
                return "14";
            case "04":
                return "14";
            case "40":
                return "14";
            case "15":
                return "15";
            case "51":
                return "15";
            case "75":
                return "15";
            case "57":
                return "15";
            case "05":
                return "15";
            case "50":
                return "15";
            default:
                Log.e("Error", "Parsed digit is not in range of 1-15");
                System.out.println("Parsed digit is not in range of 1-15");
                return "0";
        }
    }

    /**
     * Load model from directory
     * @param assetManager
     * @param modelPath
     * @throws IOException
     */
    public static void createInterpreter(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        interpreter = new Interpreter(byteBuffer);
    }

    /**
     * predicts a number with the help of the neural network
     *
     * @param bitmap bitmap to predict
     * @return result of the bitmap
     */
    public static int predict(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        float[][] result = new float[1][LABELS.size()];
        interpreter.run(byteBuffer, result);
        return getMaxValue(result[0]);
    }

    /**
     * Returns the digit with the max probability
     * @param array
     * @return
     */
    private static int getMaxValue(float[] array) {
        int idx = 0;
        if (array.length <= 0)
            throw new IllegalArgumentException("The array is empty");
        float max = array[0];
        int i;
        for (i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                idx = i;
            }
        }
        return idx;
    }
}
