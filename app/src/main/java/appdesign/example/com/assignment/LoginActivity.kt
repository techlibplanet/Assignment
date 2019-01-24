package appdesign.example.com.assignment

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import appdesign.example.com.assignment.Constants.logD
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.widget.LoginButton
import java.util.*
import com.facebook.login.LoginResult
import org.jetbrains.anko.toast


class LoginActivity : AppCompatActivity() {

    private val EMAIL = "email"
    private lateinit var loginButton: LoginButton
    private lateinit var callbackManager: CallbackManager
    private lateinit var profileTracker: ProfileTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById<View>(R.id.login_button) as LoginButton
        loginButton.setReadPermissions(Arrays.asList(EMAIL))
        // If you are using in a fragment, call loginButton.setFragment(this);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val accessToken = AccessToken.getCurrentAccessToken()
                val isLoggedIn = accessToken != null && !accessToken.isExpired
                profileTracker = object : ProfileTracker() {
                    override fun onCurrentProfileChanged(oldProfile: Profile?, currentProfile: Profile?) {
                        val firstName = currentProfile?.firstName
                        val lastName = currentProfile?.lastName
                        val id = currentProfile?.id

                        if (firstName != null) {
                            toast("Hello $firstName")
                        }
                    }
                }
                if (isLoggedIn) {
                    logD("User Logged In")
                    finish()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    logD("Login Failed")
                    toast("Login Failed")
                }
            }

            override fun onCancel() {
                // App code
                logD("On Cancel Called")
                LoginManager.getInstance().logOut()
            }

            override fun onError(exception: FacebookException) {
                // App code
                logD("Error - ${exception.message}")
                toast("Error - ${exception.message}")
            }
        })

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
