package eu.chessout.v2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

/**
 * How to sign in blog:
 * https://medium.com/@myric.september/authenticate-using-google-sign-in-kotlin-firebase-4490f71d9e44
 *
 */


private const val TAG = "chessout.v2"

class MainActivity : AppCompatActivity(), View.OnClickListener {

    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureGoogleSignIn()

        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener(this)

        firebaseAuth = FirebaseAuth.getInstance()

        signOutButton.setOnClickListener{
            signOut()
        }

    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Toast.makeText(
                this, "Congratulations. " +
                        "You are already logged in:(", Toast.LENGTH_LONG
            )
                .show()
        }
    }

    fun updateUI(account: GoogleSignInAccount?) {
        Log.d(TAG, "Time to update the ui")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.signInButton -> signIn()

        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(
                    this, "Congratulations. " +
                            "You have logged in:(", Toast.LENGTH_LONG
                )
                    .show()
            } else {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signOut() {

        Log.d(TAG, "Signing out now")
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient.signOut().addOnCompleteListener{

        }
    }
}
