package ru.zxtole.mlkit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import ru.zxtole.mlkit.ui.theme.MlkitTheme

class MainActivity : ComponentActivity() {

    private lateinit var bitmapState: MutableState<Bitmap>
    private lateinit var detector: FaceDetector
    private lateinit var croppedBitmap: Bitmap

    private companion object {
        private const val SCALING_FACTOR = 10
        private const val TAG = "FACE_DETECT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val realTimeFdo = FaceDetectorOptions.Builder()
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        detector = FaceDetection.getClient(realTimeFdo)
        val faceBitmap = BitmapFactory.decodeResource(resources, R.drawable.maxresdefault)

        setContent {
            MlkitTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Button(onClick = {
                        analyzePhoto(faceBitmap)
                    }) {
                        Text("Face")
                    }
                }
            }
        }
    }

    private fun analyzePhoto(faceBitmap: Bitmap) {

        Log.d(TAG, "analyzePhoto: ")

        val smallerBitmap = Bitmap.createScaledBitmap(
            faceBitmap,
            faceBitmap.width / SCALING_FACTOR,
            faceBitmap.height / SCALING_FACTOR,
            false
        )

        val inputImage = InputImage.fromBitmap(smallerBitmap, 0)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                Log.d(TAG, "analyzePhoto: Successfully detected face...")
                Toast.makeText(this, "Face Detected...", Toast.LENGTH_SHORT).show()

                for (face in faces) {
                    val rect = face.boundingBox
                    rect.set(
                        rect.left * SCALING_FACTOR,
                        rect.top * (SCALING_FACTOR - 1),
                        rect.right * (SCALING_FACTOR),
                        rect.bottom * SCALING_FACTOR + 90
                    )
                }

                Log.d(TAG, "analyzePhoto: number of faces ${faces.size}")
                cropDetectedFace(faceBitmap, faces)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "analyzePhoto: ", e)
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cropDetectedFace(bitmap: Bitmap, faces: List<Face>) {
        Log.d(TAG, "cropDetectedFace: ")

        //Face was detected, get cropped image as bitmap
        val rect =
            faces[1].boundingBox //there might be multiple images, if you want to get all use loop, im only managing 1

        val x = rect.left.coerceAtLeast(0)
        val y = rect.top.coerceAtLeast(0)

        val width = rect.width()
        val height = rect.height()

        croppedBitmap = Bitmap.createBitmap(
            bitmap,
            x,
            y,
            if (x + width > bitmap.width) bitmap.width.minus(x) else width,
            if (y + height > bitmap.height) bitmap.height - y else height
        )

        bitmapState = mutableStateOf(croppedBitmap)
        setContent {
            BitmapImage(bitmap = bitmapState.value)
        }
    }

    @Composable
    fun BitmapImage(bitmap: Bitmap) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "face",
        )
    }
}