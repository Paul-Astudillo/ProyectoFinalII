//package com.example.projectparteii;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.res.AssetManager;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//    private static final int REQUEST_IMAGE_CAPTURE = 1;
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//
//    private ImageView originalImageView;
//    private ImageView processedImageView;
//    private Bitmap originalBitmap;
//
//    static {
//        System.loadLibrary("projectparteii");
//    }
//
//    private native void detectFaces(Bitmap bitmap, Bitmap processedBitmap);
//    private native void initCascadePaths(String faceCascade, String eyeCascade, String noseCascade, String mouthCascade);
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Copiar archivos de assets a la carpeta de archivos de la aplicación
//        copyAssetsToFiles();
//
//        // Inicializar las rutas de los clasificadores en JNI
//        String faceCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_frontalcatface.xml";
//        String eyeCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_eye.xml";
//        String noseCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_mcs_nose.xml";
//        String mouthCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_mcs_mouth.xml";
//        initCascadePaths(faceCascadePath, eyeCascadePath, noseCascadePath, mouthCascadePath);
//
//        Button buttonTakePhoto = findViewById(R.id.button_take_photo);
//        originalImageView = findViewById(R.id.image_original);
//        processedImageView = findViewById(R.id.image_processed);
//
//        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
//                } else {
//                    dispatchTakePictureIntent();
//                }
//            }
//        });
//
//        Button buttonDetectFaces = findViewById(R.id.button_detect_faces);
//        buttonDetectFaces.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (originalBitmap != null) {
//                    Bitmap processedBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getConfig());
//                    detectFaces(originalBitmap, processedBitmap);
//                    processedImageView.setImageBitmap(processedBitmap);
//                } else {
//                    Toast.makeText(MainActivity.this, "No image to process", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    private void copyAssetsToFiles() {
//        AssetManager assetManager = getAssets();
//        String[] files = {"haarcascade_eye.xml", "haarcascade_frontalcatface.xml", "haarcascade_mcs_nose.xml", "haarcascade_mcs_mouth.xml"};
//
//        for (String filename : files) {
//            File outFile = new File(getFilesDir(), filename);
//            try (InputStream in = assetManager.open(filename); FileOutputStream out = new FileOutputStream(outFile)) {
//                byte[] buffer = new byte[1024];
//                int read;
//                while ((read = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, read);
//                }
//                Log.i(TAG, "Successfully copied " + filename);
//            } catch (IOException e) {
//                Log.e(TAG, "Failed to copy asset file: " + filename, e);
//            }
//        }
//    }
//
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                dispatchTakePictureIntent();
//            } else {
//                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            originalBitmap = (Bitmap) extras.get("data");
//            originalImageView.setImageBitmap(originalBitmap);
//        }
//    }
//}
//

package com.example.projectparteii;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 201;
    private static final int REQUEST_SELECT_IMAGE = 2;

    private ImageView originalImageView;
    private ImageView processedImageView;
    private Bitmap originalBitmap;
    private Bitmap processedBitmap;
    private String detectionResults;
    private EditText ipEditText;

    static {
        System.loadLibrary("projectparteii");
    }

    private native String detectFaces(Bitmap bitmap, Bitmap processedBitmap);
    private native void initCascadePaths(String faceCascade, String eyeCascade, String noseCascade, String mouthCascade);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Copiar archivos de assets a la carpeta de archivos de la aplicación
        copyAssetsToFiles();

        // Inicializar las rutas de los clasificadores en JNI
        String faceCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_frontalcatface.xml";
        String eyeCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_eye.xml";
        String noseCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_mcs_nose.xml";
        String mouthCascadePath = getFilesDir().getAbsolutePath() + "/haarcascade_mcs_mouth.xml";
        initCascadePaths(faceCascadePath, eyeCascadePath, noseCascadePath, mouthCascadePath);

        Button buttonTakePhoto = findViewById(R.id.button_take_photo);
        Button buttonSelectPhoto = findViewById(R.id.button_select_photo);
        originalImageView = findViewById(R.id.image_original);
        processedImageView = findViewById(R.id.image_processed);
        ipEditText = findViewById(R.id.edit_text_ip);

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

        buttonSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    selectImageFromGallery();
                }
            }
        });

        Button buttonDetectFaces = findViewById(R.id.button_detect_faces);
        buttonDetectFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalBitmap != null) {
                    processedBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getConfig());
                    detectionResults = detectFaces(originalBitmap, processedBitmap);
                    processedImageView.setImageBitmap(processedBitmap);

                    // Mostrar alerta de detección
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Detección")
                            .setMessage("Rostro(s) detectado(s) en la imagen.")
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                } else {
                    Toast.makeText(MainActivity.this, "No image to process", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button buttonSaveImage = findViewById(R.id.button_save_image);
        buttonSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    saveImageAndPositions();
                }
            }
        });

        Button buttonSendData = findViewById(R.id.button_send_data);
        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processedBitmap != null && detectionResults != null) {
                    String ipAddress = ipEditText.getText().toString();
                    if (isValidIpAddress(ipAddress)) {
                        sendImageAndData(ipAddress, processedBitmap, detectionResults);
                    } else {
                        Toast.makeText(MainActivity.this, "Dirección IP no válida", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No hay imagen procesada o resultados de detección", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void copyAssetsToFiles() {
        AssetManager assetManager = getAssets();
        String[] files = {"haarcascade_eye.xml", "haarcascade_frontalcatface.xml", "haarcascade_mcs_nose.xml", "haarcascade_mcs_mouth.xml"};

        for (String filename : files) {
            File outFile = new File(getFilesDir(), filename);
            try (InputStream in = assetManager.open(filename); FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                Log.i(TAG, "Successfully copied " + filename);
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            originalBitmap = (Bitmap) extras.get("data");
            originalImageView.setImageBitmap(originalBitmap);
        } else if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                originalBitmap = BitmapFactory.decodeStream(imageStream);
                originalImageView.setImageBitmap(originalBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageAndPositions() {
        if (processedBitmap != null && detectionResults != null) {
            // Guardar la imagen
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        Toast.makeText(this, "Imagen guardada: " + imageFileName, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error saving image", e);
                        Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                File imageFile = new File(storageDir, imageFileName);
                try (FileOutputStream out = new FileOutputStream(imageFile)) {
                    processedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Toast.makeText(this, "Imagen guardada: " + imageFileName, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Error saving image", e);
                    Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
                }
            }

            // Guardar las posiciones
            String textFileName = "POSITIONS_" + timeStamp + ".txt";
            File textFile;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, textFileName);
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/MyApp");

                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    try (OutputStream out = getContentResolver().openOutputStream(uri);
                         PrintWriter writer = new PrintWriter(out)) {
                        writer.println(detectionResults);
                        Toast.makeText(this, "Posiciones guardadas: " + textFileName, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error saving positions", e);
                        Toast.makeText(this, "Error al guardar las posiciones", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyApp");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                textFile = new File(storageDir, textFileName);
                try (PrintWriter out = new PrintWriter(textFile)) {
                    out.println(detectionResults);
                    Toast.makeText(this, "Posiciones guardadas: " + textFileName, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Error saving positions", e);
                    Toast.makeText(this, "Error al guardar las posiciones", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "No hay imagen procesada o resultados de detección", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendImageAndData(String ipAddress, Bitmap image, String detectionResults) {
        // Guardar la imagen en un archivo temporal
        String imageFileName = "temp_image.png";
        File imageFile = new File(getExternalFilesDir(null), imageFileName);
        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return;
        }

        // Enviar la imagen y los datos en un hilo secundario
        new Thread(new Runnable() {
            @Override
            public void run() {
                String serverURL = "http://" + ipAddress + ":5000/recepcion";
                try {
                    URL url = new URL(serverURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "image/png");

                    // Enviar la imagen
                    try (OutputStream outputStream = connection.getOutputStream();
                         FileInputStream fileInputStream = new FileInputStream(imageFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    // Enviar las posiciones de detección como texto
                    connection.setRequestProperty("Content-Type", "text/plain");
                    try (OutputStream outputStream = connection.getOutputStream()) {
                        outputStream.write(detectionResults.getBytes());
                    }

                    // Obtener la respuesta del servidor
                    final int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Error al enviar los datos, código: " + responseCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error al enviar los datos", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private boolean isValidIpAddress(String ipAddress) {
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9][0-9]?)$";
        return ipAddress.matches(regex);
    }
}
