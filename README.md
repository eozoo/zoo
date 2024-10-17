[![Build Status](https://github.com/cowave5/commons/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/cowave5/commons/actions)
![Static Badge](https://img.shields.io/badge/Java-17-brightgreen)
![Maven central](https://img.shields.io/badge/spring--boot-2.7.0-brightgreen)
![Maven central](https://img.shields.io/badge/maven--central-2.7.2-brightgreen)
[![License](https://img.shields.io/badge/license-Apache--2.0-brightgreen)](http://www.apache.org/licenses/LICENSE-2.0.txt)

## commons

springboot工程框架

- 工程示例 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.cowave.commons</groupId>
        <artifactId>commons-parent</artifactId>
        <version>2.7.2</version>
    </parent>

    <groupId>com.xxx</groupId>
    <artifactId>demo</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>demo</name>
    <url>https://www.cowave.com/</url>
    <description>工程示例</description>

    <dependencies>
        <dependency>
            <groupId>com.cowave.commons</groupId>
            <artifactId>commons-framework</artifactId>
        </dependency>
    </dependencies>
</project>
```

- 文档说明：[wiki](https://github.com/cowave5/commons/wiki)
- 问题建议：[issues](https://github.com/cowave5/commons/issues)
