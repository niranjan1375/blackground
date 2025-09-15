# Add project specific ProGuard rules here.

# Keep class names for crash reports
-keepattributes SourceFile,LineNumberTable

# Keep the main activity
-keep public class com.example.blackground.MainActivity {
    public *;
}