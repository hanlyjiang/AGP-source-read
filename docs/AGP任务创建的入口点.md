# AGP 插件的入口点

我们有如下问题：
1. android 的任务如何创建的？
2. 如何调试跟踪这个任务的创建过程？

## Gradle Plugin 的入口点
```JAVA
public interface Plugin<T> {
    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    void apply(T target);
}
```
Gradle 的插件都需要实现Plugin接口，在应用gradle插件时，会调用到接口的apply方法，所以apply方法就是插件的入口点。

## Android 插件的入口点
> TODO： 补充官网链接。 

插件的映射位于 `META-INF/gradle-plugins` 目录中，我们直接定位到"com.android.tools.build.gradle" 的jar包，然后查看该目录，有如下文件：
```SHELL
android.properties
android-library.properties
android-reporting.properties
com.android.application.properties
com.android.asset-pack.properties
com.android.asset-pack-bundle.properties
com.android.base.properties
com.android.dynamic-feature.properties
com.android.internal.application.properties
com.android.internal.asset-pack.properties
com.android.internal.asset-pack-bundle.properties
com.android.internal.dynamic-feature.properties
com.android.internal.library.properties
com.android.internal.reporting.properties
com.android.internal.test.properties
com.android.internal.version-check.properties
com.android.library.properties
com.android.lint.properties
com.android.reporting.properties
com.android.test.properties
```
我们打开 `com.android.application` 进行查看：
```properties
implementation-class=com.android.build.gradle.AppPlugin
```
打开 `com.android.library` 进行查看：
```properties
implementation-class=com.android.build.gradle.LibraryPlugin
```
故：
- application 插件对应到： `com.android.build.gradle.AppPlugin`
- library 插件对应到： `com.android.build.gradle.LibraryPlugin`

## 插件继承结构
```shell
Plugin<Project> 
    BasePlugin (com.android.build.gradle.internal.plugins)
        AbstractAppPlugin (com.android.build.gradle.internal.plugins)
            AppPlugin (com.android.build.gradle.internal.plugins)
            DynamicFeaturePlugin (com.android.build.gradle.internal.plugins)
        LibraryPlugin (com.android.build.gradle.internal.plugins)
        TestPlugin (com.android.build.gradle.internal.plugins)
```

### BasePlugin.pluginSpecificApply
AppPlugin 和 LibraryPlugin 都继承了 BasePlugin，我们看下 BasePlugin 实现：
```java
public abstract class BasePlugin<
                AndroidComponentsT extends
                        AndroidComponentsExtension<
                                ? extends CommonExtension<?, ?, ?, ?>,
                                ? extends ComponentBuilder,
                                ? extends Variant>,
                VariantBuilderT extends VariantBuilderImpl,
                VariantT extends VariantImpl>
        implements Plugin<Project> {

    @Override
    public final void apply(@NonNull Project project) {
        CrashReporting.runAction(
                () -> {
                    basePluginApply(project);
                    pluginSpecificApply(project);
                    project.getPluginManager().apply(AndroidBasePlugin.class);
                });
    }
    
    protected abstract void pluginSpecificApply(@NonNull Project project);
    
}
```
看起来这个方法像是 AppPlugin 的入口点，但是，当我们查看AppPlugin的实现时，发现这个方法是空的，而AppPlugin插件中的代码也很少，基本上都是override的方法。

所以我们往上看AbstractPlugin，发现其中代码更少，也全部是override的方法，所以所有的主要逻辑都在 BasePlugin 中，所以我们需要回过头来看 BasePlugin。

- BasePlugin 实现了 Gradle 的 Plugin 接口    
```java
    @Override
    public final void apply(@NonNull Project project) {
        CrashReporting.runAction(
                () -> {
                    basePluginApply(project);
                    // 都没有实现
                    pluginSpecificApply(project);
                    // AndroidBasePlugin 是一个空的插件，用于给其他插件的实现中来判断是否应用了 Android 插件(根据ID：com.android.base判断 )
                    project.getPluginManager().apply(AndroidBasePlugin.class);
                });
    }
```

## 如何判断是否应用了android 插件？
```groovy
logger.lifecycle("hasAndroidPlugin: ${pluginManager.hasPlugin('com.android.base')}")
```

```kotlin
logger.lifecycle("hasAndroidPlugin: ${pluginManager.hasPlugin("com.android.base")}")
```


## 如何在别人的基础上开发自己的框架？
1. Gradle 原先的API是什么样的？
2. AGP要实现的功能是什么样的？

## AGP 的设计
1. 为什么需要定义基础插件，基础插件的哪些功能提取出去了？为什么这样设计？



