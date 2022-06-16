# Gradle 应用实例 - 从pom中读取并添加依赖

## 需求描述

工程A依赖一个SDK B，该SDK依赖10多个SDK（C、D...),我们需要把A-N这些SDK通过fat-aar打包为一个单独的AAR。
每次B方更新时会更新一个B，同时会更新C，D，...N的版本，我们需要手动从POM文件中获取最新的版本，然后填到工程A中，然后执行fat-aar的合并操作。

## 分析
由于更新频率不算低，且手工操作繁琐易出错， 如果我们能通过代码自动获取这些依赖，可以减轻一部分工作量。

使用gradle脚本可以方便的实现，步骤比较简单：
1. 解析pom文件中的依赖；
2. 添加到 依赖中

## 实现

### groovy xml 解析
pom文件格式分析：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.github.hanlyjiang.pom</groupId>
  <artifactId>sdk</artifactId>
  <version>20220615175305-aa00d08-RELEASE</version>
  <packaging>aar</packaging>
  <description>test maven library arr.</description>
  <dependencies>
    <dependency>
      <groupId>com.google.dagger</groupId>
      <artifactId>dagger</artifactId>
      <version>2.21</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>com.github.hanlyjiang.pom</groupId>
      <artifactId>library1</artifactId>
      <version>20220526</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
</project>
```
如上pom文件中，我们只需要取出 project.dependencies 中的 dependency 即可获取所有依赖。

groovy解析xml文件可以使用 XmlSlurper 来进行：
我们提供一个函数，可以读取一个pom文件， 并获取有所依赖。
```groovy
// 从 pom 文件中读取依赖
List<GPathResult> getDependList(String fileName) {
    def file = project.file(fileName)
    if (!file.exists()) {
        logger.error("File ${file.absolutePath} does not exists!")
        return Collections.emptyList()
    }
    def projectNode = new XmlSlurper().parse(file)
    List<GPathResult> depends = new ArrayList<>()
    // 添加自己
    depends.add(projectNode)
    for (final GPathResult item in projectNode.dependencies.dependency) {
        // 添加自己的依赖
        depends.add(item)
    }
    return depends.findAll { item ->
        // 通过groupId筛选
        item.groupId == "com.github.hanlyjiang.pom"
    }
}
```

### 依赖添加
直接在 dependencies 配置段中forEach 即可添加
```groovy
def pomFile = "test.pom"

dependencies {
    // 添加依赖
    getDependList(pomFile).forEach { item ->
        implementation("${item.groupId}:${item.artifactId}:${item.version}")
    }
}
```

为了方便测试文件解析，我们可以添加一个任务用于输出列表：
```groovy
task("parsePom") {
    group = "custom"
    doLast {
        def i = 0
        getDependList(pomFile).forEach { item ->
            logger.lifecycle("${++i}.${item.groupId}:${item.artifactId}:${item.version}")
        }
    }
}
```


### 最终样子
```groovy
// GPathResult 在新版本gradle中在两个包中都有，我们使用旧的以兼容旧版本gradle
import groovy.util.slurpersupport.GPathResult


// 从 pom 文件中读取依赖
List<GPathResult> getDependList(String fileName) {
    def file = project.file(fileName)
    if (!file.exists()) {
        logger.error("File ${file.absolutePath} does not exists!")
        return Collections.emptyList()
    }
    def projectNode = new XmlSlurper().parse(file)
    List<GPathResult> depends = new ArrayList<>()
    // 添加自己
    depends.add(projectNode)
    for (final GPathResult item in projectNode.dependencies.dependency) {
        // 添加自己的依赖
        depends.add(item)
    }
    return depends.findAll { item ->
        // 通过groupId筛选
        item.groupId == "com.test.test"
    }
}

def pomFile = "test.pom"

dependencies {
    // 添加依赖
    getDependList(pomFile).forEach { item ->
        // 这里使用 embed 替换 implementation 即可支持fat-aar
        implementation("${item.groupId}:${item.artifactId}:${item.version}")
    }
}

task("parsePom") {
    group = "custom"
    doLast {
        def i = 0
        getDependList(pomFile).forEach { item ->
            logger.lifecycle("${++i}.${item.groupId}:${item.artifactId}:${item.version}")
        }
    }
}
```
执行 parsePom 输入如下：
```shell
> Task :app:parsePom
1.com.github.hanlyjiang.pom:sdk:20220526
2.com.github.hanlyjiang.pom:library1:20220526
3.com.github.hanlyjiang.pom:library2:20220526
4.com.github.hanlyjiang.pom:library3:20220526
5.com.github.hanlyjiang.pom:library4:20220526
```