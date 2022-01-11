# 泼辣相册AndroidSDK-图片评分部分
最低版本限制 Android API Level 15 (4.0.3)

## 版权限制
包含本SDK在内的所有版本库中的内容，属于Polarr, Inc.版权所有。未经允许均不得用于商业目的。当前版本的示例SDK失效时间为2022年12月31日。如需要获取完整授权等更多相关信息，请联系我们[info@polarr.co](mailto:info@polarr.co)

## 评分模块
本SDK提供图片评分功能。通过图片分析，计算出图片得分，分值从 1.0 至 5.0 （由差到好）。分析基于以下三个维度：
- 图片色彩
- 曝光度 (曝光充足或曝光不足，曝光过低或曝光过度)
- 清晰度 (图像清晰模糊的程度)

## 增加 dependencies 到 Gradle 文件
```groovy
dependencies {
    compile(name: 'processing-release', ext: 'aar')
}
```

## 图片评分接口
### 同步初始化（推荐）
请放在非UI线程中调用
```java
PolarrFeatureProcessor processor = new PolarrFeatureProcessor();
processor.initSync(context);
```
### 异步初始化
SDK会创建的处理线程
```java
PolarrFeatureProcessor processor = new PolarrFeatureProcessor();
processor.init(context);
```
### 图片评分
传入图片Bitmap和图片原始尺寸。图片需要等比例缩放，最佳大小为宽高不超过300px
- 同步调用（推荐）

请在非UI线程中调用
```java
Bitmap inputBitmap; //输入图片
Bitmap scaledBM = getScaledFitBitmap(inputBitmap, 300, 300); // 等比例缩放
FeatureItem featureResult = processor.featurePhotoSync(scaledBM, width, height); // width 和 height 为原始图片的宽高
scaledBM.recycle();
```
- 异步调用

在SDK创建的线程中调用
```java
Bitmap inputBitmap; //输入图片
Bitmap scaledBM = getScaledFitBitmap(inputBitmap, 300, 300); // 等比例缩放
processor.featurePhoto(scaledBM, width, height, new OnFeatureCallback() { // width 和 height 为原始图片的宽高
    @Override
    public void onFeature(FeatureItem featureItem) {
        // 成功回调
    }

    @Override
    public void onFailed(String reason) {
        // 失败回调以及原因
    }
});
```
### 评分结果说明
```java
FeatureItem featureResult;
// 清晰度得分 [1,5]
float metric_clarity = featureResult.metric_clarity;
// 曝光得分 [1,5]
float metric_exposure = featureResult.metric_exposure;
// 色彩得分 [1,5]
float metric_colorfulness = featureResult.metric_colorfulness;

// 总得分 [0,1]
float rating_all = featureResult.rating_all;
```
### 释放资源
```java
processor.release();
```