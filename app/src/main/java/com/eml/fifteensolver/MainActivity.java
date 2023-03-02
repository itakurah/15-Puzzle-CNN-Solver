package com.eml.fifteensolver;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import detection.ImageUtils;
import detection.openCVUtils;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;


public class MainActivity extends AppCompatActivity {
    private static Bitmap bitmap;
    private static TextView about;
    private static TextView titleLabel;
    private static TextView descLabel;
    private static TextView pickLabel;
    private static TextView appVersionLabel;
    private static TextView iconLabel;
    private static Button buttonOpenCapture;
    private static Button debug;
    private static int counterDebug = 0;
    private static final String TFLITE_NAME = "model.tflite";
    private String currentPhotoPath;
    private boolean debugmode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int versionCode = BuildConfig.VERSION_CODE;//version of app
        String versionName = BuildConfig.VERSION_NAME;//version name of app just in String
        setContentView(R.layout.activity_main);
        about = findViewById(R.id.textView5);
        about.setSoundEffectsEnabled(false);
        titleLabel = findViewById(R.id.textView2);
        descLabel = findViewById(R.id.textView4);
        pickLabel = findViewById(R.id.textView);
        appVersionLabel = findViewById(R.id.textView10);
        appVersionLabel.setText(String.format("APP_VERSION %s", versionName));
        iconLabel = findViewById(R.id.textView15);
        Linkify.addLinks(iconLabel, Linkify.WEB_URLS);
        iconLabel.setText(String.format("Icon by ProSymbols (modified)"));
        iconLabel.setOnClickListener(v -> openLink());
        about.setText("Developed by Niklas Hoefflin \nfor the module \n\"Embedded Machine Learning\" \nat the HAW Hamburg 2022");

        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        buttonOpenCapture = findViewById(R.id.button1);
        debug = findViewById(R.id.button);
        debug.setVisibility(View.INVISIBLE);
        debug.setEnabled(false);

        //create dialog box
        AlertDialog.Builder pickOption = new AlertDialog.Builder(this);
        pickOption.setTitle(R.string.dialog_title)
                .setItems(R.array.options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0) {
                            System.out.println("0");
                            openGallery();
                        } else if (which == 1) {
                            System.out.println("1");
                            openCamera();
                        }
                    }
                });
        AlertDialog dialog = pickOption.create();

        //load openCV
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV!");
            throw new IllegalArgumentException("Unable to load OpenCV!");
        } else {
            Log.d("OpenCV", "OpenCV loaded Successfully!");
        }
        //delete digits to prevent false detection
        cleanDirectories();
        //create directories
        createDirectories();
        buttonOpenCapture.setOnClickListener(v -> dialog.show());
        debug.setOnClickListener(v -> openGalleryBmp());
        about.setOnClickListener(v -> openDebugMenu());
        loadModel();

    }

    private void openLink() {
        Uri uri = Uri.parse("https://thenounproject.com/icon/neural-network-1870007/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openDebugMenu() {
        counterDebug++;
        if (counterDebug % 10 == 0) {
            Toast.makeText(getApplicationContext(), "Debug mode enabled", Toast.LENGTH_SHORT).show();
            debug.setVisibility(View.VISIBLE);
            debug.setEnabled(true);
            debugmode = true;
        } else if (debugmode) {
            Toast.makeText(getApplicationContext(), "Debug mode disabled", Toast.LENGTH_SHORT).show();
            debug.setVisibility(View.INVISIBLE);
            debug.setEnabled(false);
            debugmode = false;
        }
        if (counterDebug == 101) {
            counterDebug = 1;
        }
    }

    private ActivityResultLauncher<Intent> pickBmpFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        File galleryFile = null;
                        Uri imageUri = result.getData().getData();
                        bitmap = null;
                        try {
                            ParcelFileDescriptor parcelFileDescriptor =
                                    getContentResolver().openFileDescriptor(imageUri, "r");
                            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                            parcelFileDescriptor.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            try {
                                galleryFile = createImageFile();
                            } catch (IOException ex) {
                                Log.d(TAG, "Error occurred while creating the file");
                            }

                            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(result.getData().getData());
                            FileOutputStream fileOutputStream = new FileOutputStream(galleryFile);
                            // Copying
                            copyStream(inputStream, fileOutputStream);
                            fileOutputStream.close();
                            inputStream.close();

                        } catch (Exception e) {
                            Log.d(TAG, "onActivityResult: " + e);
                        }
                        ContentResolver contentResolver = getContentResolver();
                        try {
                            ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, result.getData().getData());
                            bitmap = ImageDecoder.decodeBitmap(source);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("ActivityGallery", "Starting image processing");
                        int prediction = ImageUtils.predict(bitmap);
                        System.out.println("RECOG" + prediction);
                        Toast.makeText(getApplicationContext(), String.valueOf(prediction), Toast.LENGTH_LONG).show();


                        File fdelete = new File(galleryFile.getPath());
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                System.out.println("file deleted :" + galleryFile.getPath());
                            } else {
                                System.out.println("file not deleted :" + galleryFile.getPath());
                            }
                        }
                    }
                }
            });
    private ActivityResultLauncher<Intent> pickImageFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        File galleryFile = null;
                        try {
                            try {
                                galleryFile = createImageFile();
                            } catch (IOException ex) {
                                Log.d(TAG, "Error occurred while creating the file");
                            }
                            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(result.getData().getData());
                            FileOutputStream fileOutputStream = new FileOutputStream(galleryFile);
                            // Copying
                            copyStream(inputStream, fileOutputStream);
                            fileOutputStream.close();
                            inputStream.close();

                        } catch (Exception e) {
                            Log.d(TAG, "onActivityResult: " + e);
                        }
                        bitmap = null;
                        ContentResolver contentResolver = getContentResolver();
                        try {
                            ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, result.getData().getData());
                            bitmap = ImageDecoder.decodeBitmap(source);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("ActivityGallery", "Starting image processing");
                        try {
                            File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/digits/");
                            //delete digits to prevent false detection
                            cleanDirectories();
                            //create directories
                            createDirectories();
                            //process image
                            processImage(galleryFile);
                        } catch (Exception e) {
                            if (e.getLocalizedMessage().equals("Attempt to read from field 'double org.opencv.core.Point.x' on a null object reference")) {
                                Toast.makeText(getApplicationContext(), "Could not detect puzzle", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }

                        File fdelete = new File(galleryFile.getPath());
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                System.out.println("file deleted :" + galleryFile.getPath());
                            } else {
                                System.out.println("file not deleted :" + galleryFile.getPath());
                            }
                        }
                    }
                }
            });
    private ActivityResultLauncher<Intent> pickImageFromCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        //create new file
                        File cameraFile = new File(currentPhotoPath);
                        if (cameraFile.exists()) {
                            Log.d("ActivityCamera", "Starting image processing");
                            try {
                                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/digits/");
                                //delete digits to prevent false detection
                                cleanDirectories();
                                //create directories
                                createDirectories();
                                //decode to bitmap
                                bitmap = BitmapFactory.decodeFile(cameraFile.getAbsolutePath());
                                System.out.println("done");
                                //process image
                                processImage(cameraFile);
                            } catch (Exception e) {
                                if (e.getLocalizedMessage().equals("Attempt to read from field 'double org.opencv.core.Point.x' on a null object reference")) {
                                    Toast.makeText(getApplicationContext(), "Could not detect puzzle", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        File fdelete = new File(cameraFile.getPath());
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                System.out.println("file deleted :" + cameraFile.getPath());
                            } else {
                                System.out.println("file not deleted :" + cameraFile.getPath());
                            }
                        }
                    }
                }
            });

    /**
     * Needed for creating file from gallery
     *
     * @param input  input stream of gallery file
     * @param output output stream of gallery file
     * @throws IOException
     */
    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    /**
     * @param image image file to be processed
     * @throws Exception
     */
    private void processImage(File image) throws Exception {
        HashMap<Integer, List<Bitmap>> tensorImages = null;
        HashMap<String, List<String>> predictionMap;
        List<Mat> listMat = new ArrayList<>();
        try {
            listMat = openCVUtils.detectGrid(image);
            tensorImages = openCVUtils.getTensorImages(listMat);
            predictionMap = ImageUtils.getGameState(tensorImages, getApplicationContext());
            Intent imageProcess = new Intent(getApplicationContext(), ImageProcessActivity.class);
            imageProcess.putExtra("hashmap", predictionMap);
//        imageProcess.putExtra("image", bitmap);
            MainActivity.this.startActivity(imageProcess);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
                System.out.println("deleted" + child);
            }
        }
        fileOrDirectory.delete();
    }

    private void openGalleryBmp() {
        Log.d("OpenGallery", "Starting Intent");
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickBmpFromGallery.launch(gallery);
    }

    private void openGallery() {
        Log.d("OpenGallery", "Starting Intent");
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageFromGallery.launch(gallery);

    }

    private void openCamera() {
        Log.d("OpenCamera", "Starting Intent");
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (camera.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                throw new IllegalArgumentException("An error occurred while creating image");
            }
            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.eml.android.fileprovider",
                        imageFile);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                pickImageFromCamera.launch(camera);
            }
        }
    }

    /**
     * Checks if user has granted app permissions
     * if not close the app
     *
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("grantPermissionResult", String.valueOf(grantResults[1]));
        System.out.println(grantResults[1]);
        if (requestCode == 1) {
            if ((grantResults[0] == PackageManager.PERMISSION_DENIED) | (grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                Toast.makeText(this, "Requested permissions are required in order to use this app", Toast.LENGTH_LONG).show();
                this.finishAffinity();
            }
        }
    }

    /**
     * Creates an image file at /Pictures/
     *
     * @return file object
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "original";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d("createImageFile", currentPhotoPath);
        System.out.println("image: " + currentPhotoPath);
        return image;
    }

    /**
     * Creates subfolder in files/Picutes/foldername
     *
     * @param folderName name
     * @return created dir
     */
    private File createSubfolder(String folderName) {
        File createdDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName);
        if (!createdDir.exists()) {
            Log.d("Info", "Dir created");
            System.out.println("Dir created");
            createdDir.mkdirs();
        } else {
            Log.d("Info", "Dir already exists");
            System.out.println("Dir already exists");
        }
        return createdDir;
    }

    /**
     * Creates subfolder in files/Picutes/parentfolder/foldername
     *
     * @param parentFolder name
     * @param folderName   name
     * @return created dir
     */
    private File createSubfolder(String parentFolder, String folderName) {
        File createdDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + parentFolder + "/", folderName);
        if (!createdDir.exists()) {
            Log.d("Info", "Dir created");
            System.out.println("Dir created");
            createdDir.mkdirs();
        } else {
            Log.d("Info", "Dir already exists");
            System.out.println("Dir already exists");
        }
        return createdDir;
    }

    private void cleanDirectories() {
        File digitsDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/digits/");
        File tensorDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/tensor_digits/");
        File markedDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pieces_marked/");
        File piecesDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/pieces/");
        //delete digits to prevent false detection
        deleteRecursive(digitsDir);
        deleteRecursive(tensorDir);
        deleteRecursive(markedDir);
        deleteRecursive(piecesDir);
    }

    private void createDirectories() {
        createSubfolder("pieces");
        createSubfolder("pieces_marked");
        File digitsDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/digits/");
        File tensorDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/tensor_digits/");
        for (int i = 1; i < 17; i++) {
            File digitsSubDir = createSubfolder("digits", String.valueOf(i));
            System.out.println(digitsSubDir.getAbsolutePath());
        }
        for (int i = 1; i < 17; i++) {
            File tensorDigitsSubDir = createSubfolder("tensor_digits", String.valueOf(i));
            System.out.println(tensorDigitsSubDir.getAbsolutePath());
        }
    }

    private void loadModel() {
        try {
            ImageUtils.createInterpreter(getAssets(), TFLITE_NAME);
        } catch (IOException e) {
            Toast.makeText(this, "Error while loading model", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


}
