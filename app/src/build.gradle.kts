plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.weibo_liweiquan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.weibo_liweiquan"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
//    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:2.9.50")
    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper:3.0.14")
    implementation("com.github.bumptech.glide:glide:4.16.0")
//    implementation("jp.wasabeef:glide-transformation:4.3.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("com.android.volley:volley:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.room:room-runtime:2.2.6")
    annotationProcessor("androidx.room:room-compiler:2.2.6")
}