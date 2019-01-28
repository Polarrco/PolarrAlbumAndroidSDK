# PolarrAlbumAndroidSDK
Polarr Android SDK for Smart Album (Polarr Album+) - Includes photo auto grouping, tagging, rating and etc. The SDK serves as an arsenal for Android developers to leverage deep learning and machine learning to organize and enhance a set of photos. Polarr currently has an iOS App called [Polarr Album+](https://itunes.apple.com/us/app/polarr-album/id1261219573?mt=8) which showcases all functions of the SDK plus some. This SDK is a subset of the functions provided by Polarr Album+, and is currently noted as an alpha release. In the coming months, more functionalities will be added and feature requests are welcomed.

This SDK includes a starter project (co.polarr.albumsdkdemo) that calls the Android SDK.

The minimum Android API Level is 15 (4.0.3).

## License
The SDK included in this repository must not be used for any commercial purposes without the direct written consent of Polarr, Inc. The current version of the SDK expires on December 31, 2019. For pricing and more info regarding the full license SDK, please email [info@polarr.co](mailto:info@polarr.co).

## Functionalities
### Tagging a photo
The SDK performs image classification and produce the top 3 most likely labels for the given photo. It also returns an overall rating value from 1.0 to 5.0 (where 1.0 is the worst and 5.0 is the best), which is based on the following metrics:
- Colorfulness
- Exposure (well-exposed photo or poorly exposed: either under-exposed or over-exposed)
- Clarity (bluriness of the photo)
- Expression (if faces are detected, are they smiling, are the eyes open)

<img src="https://user-images.githubusercontent.com/5923363/32823120-64b4fc4a-c9a1-11e7-96c8-25514ac92979.png" width="250">


### Grouping photos
Similar photos are grouped together based on their subjects, features, colors, and other metrics.
<img src="https://user-images.githubusercontent.com/5923363/32823142-81f5a192-c9a1-11e7-9c72-89a113aaaa62.png" width="250">

## Add dependencies to Gradle
```groovy
dependencies {
    // photo processing lib
    compile(name: 'processing-dev-release', ext: 'aar')
    // photo detection lib
    compile(name: 'prob_det-release', ext: 'aar')
    // Tensorflow lib
    compile 'org.tensorflow:tensorflow-android:+'
    // Face detection lib
    compile(name: 'dlib-release', ext: 'aar')
}
```

## Rating photo
Rating a photo file. The score from 1.0 to 5.0.
### Rating by file path
```java
String filePath;
boolean isBurst = false;
Map<String, Object> result = Processing.processingFile(context, filePath, isBurst);
```
### Rating by bitmap
Need a fit scaled bimap. The max width or height less then 300px. It will resize to 300px if exceeded.
```java
Bitmap bitmap;
long fileCreateTime; // millisecond.
boolean isBurst = false;
Map<String, Object> featureResult = Processing.processingFile(context, bitmap,  fileCreateTime, isBurst);
```
### Rating results
```java
float metric_clarity = (float)result.get("metric_clarity");
float metric_exposure = (float)result.get("metric_exposure");
float metric_colorfulness = (float)result.get("metric_colorfulness");
float metric_emotion = (float)result.get("metric_emotion");
  
float rating_all = (float)result.get("rating_all");
```

## Tagging photo
Recognize a photo, get top 3 possible objects from the photo
```java
// scale photo to 224x224
Bitmap bitmap = ImageUtil.getScaledBitmap(photo.getPath(), 224, 224);
Map<String, Object> taggingResult = TaggingUtil.tagPhoto(context.getAssets(), bitmap);
// marge the result with processing result
result.putAll(taggingResult);
```

## Grouping photos
First, rating and tagging photo to a feature result. Then grouping the feature results.
### Feature a photo
Join rating result and tagging result.
```java
String filePath;
boolean isBurst = false;
Map<String, Object> featureResult = Processing.processingFile(context, filePath. isBurst);
Bitmap bitmap = ImageUtil.getScaledBitmap(filePath, 224, 224);
Map<String, Object> taggingResult = TaggingUtil.tagPhoto(getAssets(), bitmap);
featureResult.putAll(taggingResult);
```
### Grouping feature results
#### init photo files
```java
List<File> realPhotos = new ArrayList<>();
```
#### Get features of photos
```java
List<Map<String, Object>> features = new ArrayList<>();
boolean isBurst = false;
for (File photo : realPhotos) {
    Map<String, Object> featureResult = Processing.processingFile(context, photo.getPath(). isBurst);
    Bitmap bitmap = ImageUtil.getScaledBitmap(photo.getPath(), 224, 224);
    Map<String, Object> taggingResult = TaggingUtil.tagPhoto(getAssets(), bitmap);
    
    featureResult.putAll(taggingResult);
    features.add(featureResult);
}
```
#### Grouping photos
```java
String identifier = "group1";
List<Map<String, Object>> features = new ArrayList<>();
boolean isBurst = false;
float sensitivity = 1f; //(0.1,1)
GroupingResultItem result = Processing.processingGrouping(identifier, features, isBurst, sensitivity, new POGenerateHClusterCallbackFunction() {
    @Override
    public void progress(double progress) {
        // grouping progress
    }
});
 ```
 #### Convert results
  ```java
 Map<Integer, List<List<Integer>>> groups = result.groups;
 int opt = result.optimalGroupIndex;
 List<List<Integer>> optGroups = groups.get(opt);
 List<List<ResultItem>> groupdFiles = new ArrayList<>();
 for (List<Integer> subGroup : optGroups) {
     List<ResultItem> sub = new ArrayList<>();
     for (Integer index : subGroup) {
         ResultItem resultItem = new ResultItem();
         resultItem.filePath = realPhotos.get(index).getPath();
         resultItem.features = features.get(index);
 
         sub.add(resultItem);
     }
     groupdFiles.add(sub);
 }
 ```
#### Get the best one
```java
ResultItem bestItem = Processing.getBest(groupdFiles);
```
#### Order photos
```java
Processing.sortGroupsByScore(groupdFiles);
```
### Order the photos with face detection
```java
List<Map<String, Object>> features = new ArrayList<>();
GroupingResultItem result = Processing.processingFaces(features);
```
