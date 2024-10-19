[![Build Status](https://github.com/cowave5/commons/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/cowave5/commons/actions)
![Static Badge](https://img.shields.io/badge/Java-17-brightgreen)
![Static Badge](https://img.shields.io/badge/spring--boot-2.7.0-brightgreen)
![Maven central](https://img.shields.io/badge/maven--central-2.7.3-brightgreen)
[![License](https://img.shields.io/badge/license-Apache--2.0-brightgreen)](http://www.apache.org/licenses/LICENSE-2.0.txt)

## commons

springboot工程框架

- 文档说明：[wiki](https://github.com/cowave5/commons/wiki)
- 问题建议：[issues](https://github.com/cowave5/commons/issues)

### 创建工程

- Mvc工程

```text
mvn archetype:generate                \
-DinteractiveMode=false               \
-DarchetypeGroupId=com.cowave.commons \
-DarchetypeArtifactId=archetype-mvc   \
-DarchetypeVersion=2.7.3              \
-DgroupId=com.cowave.demo      \
-DartifactId=demo-mvc          \
-Dversion=1.0.0                \
-Dpackage=com.cowave.demo.mvc  \
-DserverPath=/     \
-DserverPort=8080  \
-Dauthor=cowave    
```

- DDD工程

```text
mvn archetype:generate                \
-DinteractiveMode=false               \
-DarchetypeGroupId=com.cowave.commons \
-DarchetypeArtifactId=archetype-ddd   \
-DarchetypeVersion=2.7.3              \
-DgroupId=com.cowave.demo      \
-DartifactId=demo-ddd          \
-Dversion=1.0.0                \
-Dpackage=com.cowave.demo.ddd  \
-DserverPath=/     \
-DserverPort=8080  \
-Dauthor=cowave    
```

- 参数说明

```text
-DinteractiveMode       ## 是否交互式创建
-DarchetypeGroupId      ## 固定参数：模板的groupId
-DarchetypeArtifactId   ## 固定参数：模板的artifactId
-DarchetypeVersion      ## 固定参数：模板的version
-DgroupId      ## 必填参数：创建项目的groupId
-DartifactId   ## 必填参数：创建项目的artifactId
-Dversion      ## 必填参数：创建项目的version
-Dpackage      ## 必填参数：创建项目的代码package
-DserverPath   ## 选填参数：应用根路径，默认：/
-DserverPort   ## 选填参数：应用端口，默认：8080
-Dauthor       ## 选填参数：注释作者，默认：cowave
```
