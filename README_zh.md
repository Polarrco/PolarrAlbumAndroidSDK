# 泼辣相册AndroidSDK
泼辣相册 (Picky) - 通过先进的照片分析引擎对照片进行分类，打标签，打分。同时进行照片自动增强. 本SDK通过运用深度学习以及机器学习技术，提供给Android开发者照片分析以及图片增强功能。您可以在苹果应用商店下载到[《泼辣相册》](https://itunes.apple.com/cn/app/%E6%B3%BC%E8%BE%A3%E7%9B%B8%E5%86%8C/id1261219573?mt=8)。本SDK包含了泼辣相册的核心功能 

本SDK目前尚为Alpha版，包含了泼辣相册的部分功能。未来几个月内，我们将持续更新更多的功能

本SDK包含了一个示例工程 (co.polarr.albumsdkdemo) 用于调试以及开发对接

最低版本限制 Android API Level 15 (4.0.3)

## 版权限制
包含本SDK在内的所有版本库中的内容，属于Polarr, Inc.版权所有。未经允许均不得用于商业目的。当前版本的示例SDK失效时间为2018年12月31日。如需要获取完整授权等更多相关信息，请联系我们[info@polarr.co](mailto:info@polarr.co)

## 功能模块
### 评分与打标签
本SDK提供图片评分功能。通过图片分析，计算出图片得分，分值从 1.0 至 5.0 （由差到好）。分析基于以下四个维度：
- 图片色彩
- 曝光度 (曝光充足或曝光不足，曝光过低或曝光过度)
- 清晰度 (图像清晰模糊的程度)
- 表现力 (如果照片中包含人脸, 评价是否微笑或睁开眼睛)

![图片评分](https://user-images.githubusercontent.com/5923363/32823120-64b4fc4a-c9a1-11e7-96c8-25514ac92979.png)

### 图片归类
根据图片的的特点，主题，色彩，评分进行相似图片归类，优质图片排序

![图片归类](https://user-images.githubusercontent.com/5923363/32823142-81f5a192-c9a1-11e7-9c72-89a113aaaa62.png)

## 增加 dependencies 到 Gradle 文件
```groovy
dependencies {
    // 图片优选核心模块
    compile(name: 'processing-dev-release', ext: 'aar')
    // 人脸识别模块
    compile(name: 'dlib-release', ext: 'aar')
    // 图片分析辅助模块
    compile(name: 'prob_det-release', ext: 'aar')
    // TensorFlow运行库，用于图片分析
    compile 'org.tensorflow:tensorflow-android:+'
}
```

## 图片评分接口
给一个图片文件评分，分值从 1.0 至 5.0
### 传入图片文件路径
```java
// 图片路径
String filePath;
// 是否快速模式（不分析人物表情）
boolean isBurst = false;
Map<String, Object> result = Processing.processingFile(context, filePath, isBurst);
```
### 传入图片Bitmap和图片文件创建时间
图片需要等比例缩放，最佳大小为宽高不超过300px
```java
Bitmap bitmap;
long fileCreateTime;// 精确到 ms 毫秒
// 是否快速模式（不分析人物表情）
boolean isBurst = false;
Map<String, Object> featureResult = Processing.processingFile(context, bitmap,  fileCreateTime, isBurst);
```
### 评分结果说明
```java
// 清晰度得分
float metric_clarity = (float)result.get("metric_clarity");
// 曝光得分
float metric_exposure = (float)result.get("metric_exposure");
// 色彩得分
float metric_colorfulness = (float)result.get("metric_colorfulness");
// 表情得分
float metric_emotion = (float)result.get("metric_emotion");
  
// 总得分
float rating_all = (float)result.get("rating_all");
```

## 获取图片标签，用于图片精准分类
```java
// 将图片缩放到224x224大小
Bitmap bitmap = ImageUtil.getScaledBitmap(photo.getPath(), 224, 224);
// 获取图片标签
Map<String, Object> taggingResult = TaggingUtil.tagPhoto(context.getAssets(), bitmap);
// 将标签结果和评分结果合并
result.putAll(taggingResult);
```

## 图片归类
先对每张图片进行评分，打标签获取图片特性，之后对图片进行归类
### 获取图片特性
图片特性包括评分和打标签两部分组成
```java
String filePath;
// 是否快速模式（不分析人物表情）
boolean isBurst = false;
Map<String, Object> featureResult = Processing.processingFile(context, filePath. isBurst);
Bitmap bitmap = ImageUtil.getScaledBitmap(filePath, 224, 224);
Map<String, Object> taggingResult = TaggingUtil.tagPhoto(getAssets(), bitmap);
featureResult.putAll(taggingResult);
```
### 图片归类接口（图片回忆）
#### 初始化图片文件
```java
List<File> realPhotos = new ArrayList<>();
```
#### 获取图片属性信息
```java
List<Map<String, Object>> features = new ArrayList<>();
boolean isBurst = false;
for (File photo : realPhotos) {
    Map<String, Object> featureResult = Processing.processingFile(context, photo.getPath(). isBurst);
    // 连拍归档不需要进行图片标签分析
    Bitmap bitmap = ImageUtil.getScaledBitmap(photo.getPath(), 224, 224);
    Map<String, Object> taggingResult = TaggingUtil.tagPhoto(getAssets(), bitmap);
    
    featureResult.putAll(taggingResult);
    features.add(featureResult);
}
```
#### 图片分组，包含连拍归档
```java
String identifier = "group1";
// 上一步获取的包含所有图片属性的数组
List<Map<String, Object>> features = new ArrayList<>();
// 是否快速模式，用于连拍归档
boolean isBurst = false;
// 分组敏感度，数值越低越不敏感。最佳值为1.0，取值范围为 0.01, 1。
float sensitivity = 1f; //(0.01, 1.0)
GroupingResultItem result = Processing.processingGrouping(identifier, features, isBurst, sensitivity, new POGenerateHClusterCallbackFunction() {
    @Override
    public void progress(double progress) {
        // grouping progress
    }
});
 ```
#### 将结果转化为对象
 ```java
// 分组结果
Map<Integer, List<List<Integer>>> groups = result.groups;
// 最佳结果的索引
int opt = result.optimalGroupIndex;
// 获得最佳的分组结果
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
#### 获取最佳照片
```java
ResultItem bestItem = Processing.getBest(groupdFiles);
```
#### 按评价排列图片
```java
Processing.sortGroupsByScore(groupdFiles);
```
### 人物封面
```java
// 包含所有图片属性的数组
List<Map<String, Object>> features = new ArrayList<>();
GroupingResultItem result = Processing.processingFaces(features);
```
后续参考：[将结果转化为对象](#将结果转化为对象)

## 用第三方人脸识别库进行表情评分
用第三方表情识别库返回的数据进行表情评分

人脸数据，支持多张人脸，每张人脸数据的点个数必须为106个
```java
List<List<PointF>> facePoints = new ArrayList<>();
List<Rect> faceRects = new ArrayList<>();
```
先进行[Processing.processingFile](#图片评分接口)处理，之后做表情评分分析。 如果为 `featureResult==null` 则表示只做表情评分，此时有返回值。
```java
Map<String, Object> featureResult;
```
如果进行人脸识别的图片尺寸与原始图片尺寸不一致，则需要调用这个接口
```java
Processing.computeEmotion(featureResult, facePoints, faceRects, FACE_DET_WIDTH, FACE_DET_HEIGHT);
```
如果进行人脸识别的图片尺寸与原始图片尺寸相同，则调用这个接口即可。此时必须要先进行 [Processing.processingFile](#图片评分接口)
```java
Processing.computeEmotion(featureResult, facePoints, faceRects);
```
