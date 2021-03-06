package tech.lincaiqi.phototimefix.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.lincaiqi.phototimefix.R
import tech.lincaiqi.phototimefix.databinding.ExperimentalFunctionBinding
import java.io.FileDescriptor

fun initFragment(
    preferences: SharedPreferences,
    editor: SharedPreferences.Editor,
    radioGroup: RadioGroup,
) {
    radioGroup.check(preferences.getInt("mode", R.id.radioButton))
    radioGroup.setOnCheckedChangeListener { _, i ->
        editor.putInt("mode", i)
        editor.apply()
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun showAbout(context: Activity) {
    val builder = AlertDialog.Builder(context)
    try {
        //val view = LayoutInflater.from(context).inflate(R.layout.about, null)
        val webView = WebView(context)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/about.html")
        webView.settings.javaScriptEnabled = true
        webView.requestFocusFromTouch()
        webView.addJavascriptInterface(JavaScriptBridge(context), "openGit")
        builder.setView(webView)
    } catch (e: Exception) {
        builder.setMessage(context.getString(R.string.webviewError))
        e.printStackTrace()
    }
    builder.setPositiveButton(context.getString(R.string.OK), null)
    builder.show()
}

suspend fun updateImage(uri: Uri?, activity: Activity) {
    val imageView: ImageView = activity.findViewById(R.id.user_bg)
    if (uri != null) {
        val image: Bitmap
        withContext(Dispatchers.IO) {
            val parcelFileDescriptor = activity.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
        }
        withContext(Dispatchers.Main) {
            imageView.setImageBitmap(image)
            updateAppbar(activity, false)
        }
    } else {
        imageView.setImageBitmap(null)
        updateAppbar(activity, true)
    }
}

fun updateAppbar(activity: Activity, scrollAble: Boolean) {
    val mAppBarLayout = activity.findViewById<AppBarLayout>(R.id.app_bars)
    val mAppBarChildAt: View = mAppBarLayout.getChildAt(0)
    val mAppBarParams: AppBarLayout.LayoutParams = mAppBarChildAt.layoutParams as AppBarLayout.LayoutParams
    mAppBarParams.scrollFlags = if (scrollAble) AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    else 0
    mAppBarChildAt.layoutParams = mAppBarParams
    /* 作者：Silas_
       来源：CSDN
       原文：https://blog.csdn.net/qq_31852701/article/details/80859644
       版权声明：本文为博主原创文章，转载请附上博文链接！ */
}

fun experimentalFunction(context: Activity) {
    val binding = ExperimentalFunctionBinding.inflate(LayoutInflater.from(context))
    val switch = binding.switch1
    switch.isChecked = context.getPreferences(Context.MODE_PRIVATE).getBoolean("useEXIF", false)
    binding.switchIconButton.setOnClickListener { switchIcon(context) }
    val editor = context.getPreferences(Context.MODE_PRIVATE).edit()
    binding.root.setMargins()
    MaterialAlertDialogBuilder(context).setTitle(R.string.experimentalFunction).setView(binding.root)
        .setPositiveButton(R.string.OK) { _, _ ->
            editor.putBoolean("useEXIF", switch.isChecked)
            editor.apply()
        }.show()
}

fun switchIcon(activity: Activity) {
    val defaultComponentName = ComponentName(activity.baseContext, "tech.lincaiqi.PhotoTimeFix.ui.MainActivity")
    val newComponentName = ComponentName(activity.baseContext, "photoTimeFix.newIcon")
    val packageManager = activity.packageManager
    AlertDialog.Builder(activity).setTitle(R.string.switchIcon).setMessage(R.string.okToUseNew)
        .setPositiveButton(R.string.OK) { _, _ ->
            packageManager.setComponentEnabledSetting(defaultComponentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            packageManager.setComponentEnabledSetting(newComponentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }.setNegativeButton(R.string.cancel) { _, _ ->
            packageManager.setComponentEnabledSetting(newComponentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            packageManager.setComponentEnabledSetting(defaultComponentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }.show()
}

fun Context.toast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
}

fun Context.toast(res: Int) {
    Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(string: String) {
    Toast.makeText(this, string, Toast.LENGTH_LONG).show()
}

fun Context.longToast(res: Int) {
    Toast.makeText(this, res, Toast.LENGTH_LONG).show()
}

fun View.setMargins() {
    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    params.setMargins(16, 0, 16, 0)
    layoutParams = params
}

fun showErrorDialog(string: String, context: Context) {
    MaterialAlertDialogBuilder(context).setTitle(R.string.error).setMessage(string).setPositiveButton(R.string.OK, null)
}