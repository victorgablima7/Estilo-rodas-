[app]

# (str) Title of your application
title = WheelSwap AI

# (str) Package name
package.name = wheelswapai

# (str) Package domain (needed for android/ios packaging)
package.domain = org.wheelswap

# (str) Source code where the main.py lives
source.dir = .

# (list) Source files to include (let empty to include all the files)
source.include_exts = py,png,jpg,kv,atlas,spec

# (str) Application versioning (method 1)
version = 1.0.0

# (list) Application requirements
# comma separated e.g. requirements = sqlite3,kivy
requirements = python3, kivy, google-generativeai, pillow, requests, urllib3, charset-normalizer, idna

# (str) Supported orientation (one of landscape, sensorLandscape, portrait or all)
orientation = portrait

# (bool) Indicate if the application should be fullscreen
fullscreen = 0

# (list) Permissions
android.permissions = INTERNET, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE

# (int) Target Android API, should be as high as possible.
android.api = 34

# (int) Minimum API your APK / AAB will support.
android.minapi = 24

# (str) Android NDK version to use
# android.ndk = 25b

# (bool) Use --private data storage (True) or --dir public storage (False)
android.private_storage = True

# (list) List of Java .jar files to add to the libs so that pyjnius can access
# their classes. Don't add jars that you do not need, since extra jars can slow
# down the build process.
# android.add_jars = foo.jar,bar.jar,path/to/more/*.jar

# (list) List of Gradle dependencies to add
# android.gradle_dependencies =

# (bool) Enable AndroidX support. Enable when you use Kotlin or any library that requires AndroidX.
android.enable_androidx = True

# (list) Android application meta-data to set (key=value format)
# android.meta_data =

# (list) Android app theme to apply.
# android.apptheme = "@android:style/Theme.NoTitleBar"

[buildozer]

# (int) Log level (0 = error only, 1 = info, 2 = debug (with command output))
log_level = 2

# (int) Display warning if buildozer is run as root (0 = False, 1 = True)
warn_on_root = 1
