# 图像超分辨率服务

这是一个基于 Spring Boot 的图像超分辨率 Web 服务，使用 TorchServe 进行模型推理，并将处理后的图片保存到阿里云 OSS。

## 项目结构
imageSR-web/
├── common/ # 通用模块
│ └── src/main/java/
│ └── com/ws/common/
│ ├── config/ # 配置类
│ └── utils/ # 工具类
└── super-resolution-service/ # 超分辨率服务模块
└── src/main/java/ws/
├── controller/ # 控制器
├── service/ # 服务接口及实现
└── SuperResolutionApplication.java # 启动类


## 技术栈

- Spring Boot
- Spring Cloud Alibaba
- Nacos (服务发现)
- OkHttp3
- Aliyun OSS
- TorchServe

## 快速开始

### 1. 环境要求

- JDK 11+
- Maven 3.6+
- Nacos 服务器
- TorchServe 服务器
- 阿里云 OSS 账号

### 2. 配置


