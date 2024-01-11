# Polar Album Android SDK - Photo rating part
The minimum Android API Level is 15 (4.0.3).

## License
The SDK included in this repository must not be used for any commercial purposes without the direct written consent of Polarr, Inc. The current version of the SDK expires on December 31, 2024. For pricing and more info regarding the full license SDK, please email [info@polarr.co](mailto:info@polarr.co).

## Functionalities
The SDK performs an overall rating value from 1.0 to 5.0 (where 1.0 is the worst and 5.0 is the best), which is based on the following metrics:
- Colorfulness
- Exposure (well-exposed photo or poorly exposed: either under-exposed or over-exposed)
- Clarity (bluriness of the photo)

## Add dependencies to Gradle
```groovy
dependencies {
    compile(name: 'processing-release', ext: 'aar')
}
```

## Rating photo
### Synchronized initialization (recommanded)
Runs on non-UI thread.
```java
PolarrFeatureProcessor processor = new PolarrFeatureProcessor();
processor.initSync(context);
```
### Ansynchronized initialization
SDK generates a new thread.
```java
PolarrFeatureProcessor processor = new PolarrFeatureProcessor();
processor.init(context);
```
### Rating
Set up a bitmap and the original width and height of the photo. 
Please downscale the bitmap as pre-processing. The recommand size is less than 300px width or height.
- Synchronized (recommanded)
Runs on non-UI thread.
```java
Bitmap inputBitmap;
Bitmap scaledBM = getScaledFitBitmap(inputBitmap, 300, 300); // downscaled with same ration (w:h)
FeatureItem featureResult = processor.featurePhotoSync(scaledBM, width, height); // original width and original height
scaledBM.recycle();
```
- Ansynchronized
```java
Bitmap inputBitmap;
Bitmap scaledBM = getScaledFitBitmap(inputBitmap, 300, 300); // downscaled with same ration (w:h)
processor.featurePhoto(scaledBM, width, height, new OnFeatureCallback() { // original width and original height
    @Override
    public void onFeature(FeatureItem featureItem) {
        // successful
    }

    @Override
    public void onFailed(String reason) {
        // failed
    }
});
```
### The intro of the result
```java
FeatureItem featureResult;
// Clarity score [1,5]
float metric_clarity = featureResult.metric_clarity;
// Exposure score [1,5]
float metric_exposure = featureResult.metric_exposure;
// Colorfulness score [1,5]
float metric_colorfulness = featureResult.metric_colorfulness;

// Total score [0,1]
float rating_all = featureResult.rating_all;
```
### Release resource
```java
processor.release();
```