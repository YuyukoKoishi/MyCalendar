# Keep activities referenced from manifest
-keep class com.example.mycalendar.MainActivity { *; }
-keep class com.example.mycalendar.NoteEditActivity { *; }
-keep class com.example.mycalendar.NoteListActivity { *; }
-keep class com.example.mycalendar.DatePickerDialog { *; }

# Android framework
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Dialog
-keep public class * extends android.widget.BaseAdapter

# View inflation from XML
-keep public class * extends android.view.View { public <init>(...); }

-dontwarn android.**
