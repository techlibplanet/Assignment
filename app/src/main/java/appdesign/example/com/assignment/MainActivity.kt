package appdesign.example.com.assignment

import android.Manifest
import android.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.facebook.login.LoginManager
import com.intsig.csopen.sdk.*
import net.rmitsolutions.libcam.LibCamera
import net.rmitsolutions.libcam.LibPermissions
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity() : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var camScanner: Button
    private lateinit var libPermissions: LibPermissions
    private lateinit var libCamera: LibCamera
    private var imageUri: Uri? = null
    val TAKE_PHOTO = 201
    val CROP_PHOTO = 203
    private lateinit var imageView: ImageView

    private val DIR_IMAGE = Environment.getExternalStorageDirectory().absolutePath

    private var mApi: CSOpenAPI? = null
    private val REQ_CODE_CALL_CAMSCANNER = 2


    private var mSourceImagePath: String? = null
    private var mOutputImagePath: String? = null
    private var mOutputPdfPath: String? = null
    private var mOutputOrgPath: String? = null
    private var mBitmap: Bitmap? = null

    val permissions = arrayOf<String>(Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
    private lateinit var api: CSOpenAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mApi = CSOpenApiFactory.createCSOpenApi(this, Constants.APP_KEY, null)

        libCamera = LibCamera(this)

        libPermissions = LibPermissions(this, permissions)
        imageView = findViewById(R.id.imageView)
        camScanner = findViewById(R.id.buttonCamScanner)

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



    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        mOutputImagePath = savedInstanceState.getString(SCANNED_IMAGE)
        mOutputPdfPath = savedInstanceState.getString(SCANNED_PDF)
        mOutputOrgPath = savedInstanceState.getString(ORIGINAL_IMG)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SCANNED_IMAGE, mOutputImagePath)
        outState.putString(SCANNED_PDF, mOutputPdfPath)
        outState.putString(ORIGINAL_IMG, mOutputOrgPath)
        super.onSaveInstanceState(outState)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(Tag, "requestCode:$requestCode resultCode:$resultCode")
        if (requestCode == REQ_CODE_CALL_CAMSCANNER) {
            mApi!!.handleResult(requestCode, resultCode, data, object : CSOpenApiHandler {

                override fun onSuccess() {
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("Success")
                            .setMessage("Open API call Success")
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show()
                    mBitmap = Util.loadBitmap(mOutputImagePath!!)
                }

                override fun onError(errorCode: Int) {
                    val msg = handleResponse(errorCode)
                    AlertDialog.Builder(this@MainActivity)
                            .setTitle("Fail")
                            .setMessage(msg)
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show()
                }

                override fun onCancel() {
                    AlertDialog.Builder(this@MainActivity)
                            .setMessage("User Cancel")
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show()
                }
            })
        }

        if (requestCode == TAKE_PHOTO) {
            if (data != null) {
                val u = data.data
                val c = contentResolver.query(u!!, arrayOf("_data"), null, null, null)
                if (c == null || c.moveToFirst() == false) {
                    return
                }
                mSourceImagePath = c.getString(0)
                c.close()
                imageView.setImageURI(Uri.parse(mSourceImagePath))
                go2CamScanner()
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

    private fun go2CamScanner() {
        mOutputImagePath = "${DIR_IMAGE}/scanned.jpg"
        mOutputPdfPath = "${DIR_IMAGE}/scanned.pdf"
        mOutputOrgPath = "${DIR_IMAGE}/org.jpg"
        try {
            val fos = FileOutputStream(mOutputOrgPath!!)
            fos.write(3)
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val param = CSOpenAPIParam(mSourceImagePath,
                mOutputImagePath, mOutputPdfPath, mOutputOrgPath, 1.0f)
        val res = mApi!!.scanImage(this, REQ_CODE_CALL_CAMSCANNER, param)
        Log.d(Tag, "send to CamScanner result: $res")
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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

    private fun handleResponse(code: Int): String {
        when (code) {
            ReturnCode.OK -> return "Open API call Success"
            ReturnCode.INVALID_APP -> return "Invalid APP ID"
            ReturnCode.INVALID_SOURCE -> return "Invalid image resouce"
            ReturnCode.AUTH_EXPIRED -> return "Authorization Expired"
            ReturnCode.MODE_UNAVAILABLE -> return "The choosen enhance mode is unavailable for this app_id"
            ReturnCode.NUM_LIMITED -> return "Over the request number limit"
            ReturnCode.STORE_JPG_ERROR -> return "CamScanner can\\'t return scanned image file"
            ReturnCode.STORE_PDF_ERROR -> return "CamScanner can\\'t return scanned PDF file"
            ReturnCode.STORE_ORG_ERROR -> return "CamScanner can\\'t return original image file"
            ReturnCode.APP_UNREGISTERED -> return "CamScanner can\\'t return scanned file"
            ReturnCode.API_VERSION_ILLEGAL -> return "This app is not regisered"
            ReturnCode.DEVICE_LIMITED -> return "Over the device number limit"
            ReturnCode.NOT_LOGIN -> return "CamScanner user do not login"
            else -> return "Return code = $code"
        }
    }


    companion object {

        private val Tag = "MainActivity"

        private val APP_KEY = "KQEH6HfhePJaQd8h73TUA8HP"

        private val DIR_IMAGE = Environment.getExternalStorageDirectory().absolutePath + "/CSOpenApiDemo"

        // three values for save instance;
        private val SCANNED_IMAGE = "scanned_img"
        private val SCANNED_PDF = "scanned_pdf"
        private val ORIGINAL_IMG = "ori_img"
    }

}
