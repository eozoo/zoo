#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_pound}${symbol_pound} ${artifactId}

${symbol_pound}${symbol_pound}${symbol_pound} 工程目录

> 目录中标记了（默认）的文件，在mvn compile后，可以在target中目录中找到；

<pre>
${artifactId}
   ├─bin
   │  ├─env.properties                         ${symbol_pound}${symbol_pound} 环境变量定义                （默认，可覆盖）
   │  ├─setenv.sh                              ${symbol_pound}${symbol_pound} 运行前设置                 （默认，可覆盖）
   │  ├─run.sh                                 ${symbol_pound}${symbol_pound} 运行脚本                   （默认，可覆盖）
   │  └─install.sh                             ${symbol_pound}${symbol_pound} Tar包安装脚本              （默认，可覆盖）
   ├─src    
   │  └─main    
   │     ├─java    
   │     │  └─${package}
   │     │     ├─...
   │     │     └─Application.java
   │     │
   │     └─resources
   │        ├─smart-doc.json                   ${symbol_pound}${symbol_pound} smart-doc接口文档描述
   │        ├─logback-spring.xml               ${symbol_pound}${symbol_pound} logback日志配置            （默认，可覆盖）
   │        ├─sql                              ${symbol_pound}${symbol_pound} liquibase数据库版本管理
   │        │  ├─changelog.yml
   │        │  ├─...
   │        ├─config                           ${symbol_pound}${symbol_pound} 应用配置（约定放在config目录中，使用yml格式）
   │        │  ├─application.yml    
   │        │  ├─...    
   │        └─META-INF    
   │           ├─common.yml                    ${symbol_pound}${symbol_pound} 默认配置
   │           └─i18n                          ${symbol_pound}${symbol_pound} 国际化资源
   │              ├─...    
   │
   ├─tar.sh                                    ${symbol_pound}${symbol_pound} Tar构建                   （默认，可覆盖）
   ├─deb.sh                                    ${symbol_pound}${symbol_pound} Deb构建                   （默认，可覆盖）
   ├─docker.sh                                 ${symbol_pound}${symbol_pound} Docker构建                （默认，可覆盖）
   ├─pom.xml    
   ├─favicon.ico                               ${symbol_pound}${symbol_pound} 网页图标                   （默认，可覆盖）
   └─README.md   
</pre>

- 应用打包

`maven clean install -Dbuild=jar` springboot默认的打包方式，整个打成一个jar包，使用命令行自行启动；    
`maven clean install -Dbuild=tar` 打成Tar包，针对Linux环境下的裸机安装；    
`maven clean install -Dbuild=deb` 打成Deb包，针对ubuntu环境下的裸机安装；    
`maven clean install -Dbuild=docker` 打成Docker镜像
