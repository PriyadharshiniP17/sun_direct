# Eclude KOP4J on non relevant code
# ----------------------------------
-kop-exclude com.google.**
-kop-exclude com/flurry/sdk/*.class
-kop-exclude com/crashlytics/android/answers/shim/*.class
-kop-exclude io/branch/referral/*

# Java only protection (no native library)
# ----------------------------------------
-kop-hash-strategy java

# Apply flattening ... on logData method... must be extended later
# -----------------------------------------------------------------
-kop-flatten,enable public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
#-kop-flatten,enable public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }

# Apply String masking on logData method only ... must be extended later
# ---------------------------------------------------------------------
-kop-mask-string,disable class * { ; }
-kop-mask-string,enable public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
#-kop-mask-string,enable public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }

# Apply call masking on logData method only ... must be extended later
# ---------------------------------------------------------------------
-kop-accessthroughreflection public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
#-kop-accessthroughreflection public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }


# Do checks in immedate, in-app moniroring to add later
# -----------------------------------------------------
-kop-check-dex-signature,immediate,statusField:com.myplex.myplex.ui.activities.LoginActivity.dexSignatureStatusBitField public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
-kop-shelf-life-check,immediate,date:2023-10-10,statusField:com.myplex.myplex.ui.activities.LoginActivity.dateStatusBitField public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
-kop-check-exe-context,immediate,statusField:com.myplex.myplex.ui.activities.LoginActivity.contextStatusBitField public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
-kop-check-manifest,immediate,statusField:com.myplex.myplex.ui.activities.LoginActivity.manifestStatusBitField public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
-kop-check-certificate,immediate,statusField:com.myplex.myplex.ui.activities.LoginActivity.certificateStatusBitField public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
#-kop-check-installers,installers:com.android.vending,immediate public class com.myplex.myplex.ui.activities.LoginActivity { public static void logData(); }
-kop-report kopReport.md

#Somemore checks with immediate MainActivity Video Activty

#-kop-check-dex-signature,immediate,statusField:com.myplex.myplex.ui.activities.MainActivity.dexSignatureStatusBitField public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }
#-kop-shelf-life-check,immediate,date:2023-10-10,statusField:com.myplex.myplex.ui.activities.MainActivity.dateStatusBitField public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }
#-kop-check-exe-context,immediate,statusField:com.myplex.myplex.ui.activities.MainActivity.contextStatusBitField public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }
#-kop-check-manifest,immediate,statusField:com.myplex.myplex.ui.activities.MainActivity.manifestStatusBitField public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }
#-kop-check-certificate,immediate,statusField:com.myplex.myplex.ui.activities.MainActivity.certificateStatusBitField public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }
#-kop-check-installers,installers:com.android.vending,immediate public class com.myplex.myplex.ui.activities.MainActivity { public static void logMainData(); }

-kop-check-dex-signature-resource-path res/raw/a.txt
-kop-check-dex-integrity-resource-path res/raw/b.txt