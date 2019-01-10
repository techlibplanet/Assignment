package appdesign.example.com.assignment

import android.Manifest
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.facebook.login.LoginManager
import net.rmitsolutions.libcam.LibCamera
import net.rmitsolutions.libcam.LibPermissions
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_CODE = 420
    private lateinit var camScanner: Button
    private lateinit var buttonCameraScanner: Button
    private lateinit var libPermissions: LibPermissions
    private lateinit var libCamera: LibCamera
    private var imageUri: Uri? = null
    val TAKE_PHOTO = 201
    val CROP_PHOTO = 203
    private lateinit var imageView: ImageView

    val permissions = arrayOf<String>(Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        libCamera = LibCamera(this)

        libPermissions = LibPermissions(this, permissions)
        imageView = findViewById(R.id.imageView)
        camScanner = findViewById(R.id.buttonCamScanner)
        buttonCameraScanner = findViewById(R.id.buttonCameraScanner)

        val runnable = Runnable {
            logD("All permission enabled")
        }
        libPermissions.askPermissions(runnable)

        camScanner.setOnClickListener {

            val runnable = Runnable {
                libCamera.takePhoto()
            }
            libPermissions.askPermissions(runnable, "android.permission.CAMERA")
        }

        buttonCameraScanner.setOnClickListener {
            val intent = Intent("com.intsig.camscanner.ACTION_SCAN")
//            // Or content uri picked from gallery
//            //val uri = Uri.fromFile(File(imageUri?.path))
            intent.putExtra(Intent.EXTRA_STREAM, imageUri?.path)
            val path = libCamera.savePhotoInDeviceMemory(imageUri!!,"scanned",false)
//            //val file = File(filesDir, "scanned.jpg")
            intent.putExtra("scanned_image", path)
//
//            //intent.putExtra("pdf_path", libCamera.savePhotoInDeviceMemory(imageUri!!,"processed",false))
//            //intent.putExtra("org_image", libCamera.savePhotoInDeviceMemory(imageUri!!,"org",false))
            startActivityForResult(intent, REQUEST_CODE)

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode === REQUEST_CODE) {
            logD("ResultCode - $resultCode RequestCode - $requestCode")
            val resultCode = data?.getIntExtra("RESULT_OK", -1)
            logD("RESULT_OK - $resultCode")
            val responseCode = data?.getIntExtra("RESPONSE_CODE", -1)
            logD("Resposne code - $responseCode")
            if (requestCode === Activity.RESULT_OK) {
                logD("represents CamScanner has accepted your request and processed the image.")
                toast("CamScanner accept the request and processed the image")
                // Success
            } else if (resultCode === Activity.RESULT_FIRST_USER) {
                logD("represents CamScanner has denied your request and will not return you the processed file")
                toast("CamScanner denied the request")
                // Fail
            } else if (resultCode === Activity.RESULT_CANCELED) {
                logD("represents the user has canceled the scanning process.")
                toast("User cancelled the scanning progress")
                // User canceled
            }
        }
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                val resultImageUri = libCamera.getPickImageResultUri(data)
                imageUri = resultImageUri!!
                logD("Result Image Uri - ${resultImageUri.path}")
                imageView.setImageURI(resultImageUri)
                //libCamera.cropImage(imageUri!!)
                //logD("Uri - ${libCamera.savePhotoInDeviceMemory(imageUri!!,"scanned",false)}")


            }
        }

        if (requestCode == CROP_PHOTO) {
            if (data != null) {
                imageUri = libCamera.cropImageActivityResult(requestCode, resultCode, data)!!
                logD("Crop Image Uri - ${imageUri!!.path}")
                imageView.setImageURI(imageUri)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.icon_crop -> {
                if (imageUri!=null){
                    libCamera.cropImage(imageUri!!)
                }else{
                    toast("Add photo to crop")
                }
            }

            R.id.ic_logout -> {
                LoginManager.getInstance().logOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun logD(message: String) {
        Log.d(TAG, message)
    }
}
