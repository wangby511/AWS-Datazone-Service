# ☁️ AWS DataZone 模拟服务 (Java Backend)

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Maven-blue)](https://maven.apache.org/)
[![AWS](https://img.shields.io/badge/AWS-Lambda%20%7C%20DynamoDB-yellow)](https://aws.amazon.com/)

这是一个基于 AWS Lambda 和 DynamoDB 构建的后端服务，旨在模拟 AWS DataZone 的核心资源管理功能。它提供了 **Domain (领域)** 和 **Project (项目)** 的层级管理能力，采用 RESTful API 设计。

## 📚 目录
- [架构设计](#-架构设计)
- [快速开始](#-快速开始)
- [API 文档](#-api-文档)
  - [Domain 接口](#1-domain-接口)
  - [Project 接口](#2-project-接口)
- [开发指南](#-开发指南)

## 🏗 架构设计

* **计算层**: AWS Lambda (Java 17) 接收 API Gateway 的代理请求。
* **分发层**: `EnvironmentDispatcher` 负责根据 URI 和 HTTP Method 将请求路由到具体的 Handler。
* **存储层**: Amazon DynamoDB (使用 Enhanced Client)。
    * **Domain表**: 存储领域信息，主键为 `identifier`。
    * **Project表**: 存储项目信息，主键为 `id`，通过 GSI `by-domain` 关联 Domain。

## 🚀 快速开始

### 先决条件
* Java JDK 17+
* Apache Maven 3.8+

### 构建项目
在当前目录下运行 Maven 命令打包 Fat Jar：

```bash
mvn clean install
```

> 构建成功后，`target/` 目录下会生成 `aws-datazone-api-1.0-SNAPSHOT.jar`。

### 运行测试
执行所有单元测试（JUnit 5 + Mockito）：

```bash
mvn test
```

---

## 📖 API 文档

### 1. Domain 接口
**Base Path:** `/domains`

| 方法 | URI | 描述 | 请求/响应示例 |
| :--- | :--- | :--- | :--- |
| **POST** | `/` | 创建 Domain | **Body:** `{ "name": "Sales", "description": "Sales Data", "domainExecutionRole": "arn:aws:iam::123:role/ExecRole" }`<br>**Resp:** `{ "identifier": "dzd-...", ... }` |
| **GET** | `/` | 查询列表 | **Params:** `?maxResults=10&nextToken=...`<br>**Resp:** `{ "items": [...], "nextToken": "..." }` |
| **GET** | `/{identifier}` | 获取详情 | **Path:** `identifier` (e.g., `dzd-abc-123`)|
| **DELETE** | `/{identifier}` | 删除 Domain | **Resp:** 204 No Content |

### 2. Project 接口
**Base Path:** `/domains/{domainIdentifier}/projects`

| 方法 | URI | 描述 | 请求/响应示例 |
| :--- | :--- | :--- | :--- |
| **POST** | `/` | 创建 Project | **Body:** `{ "name": "Q1 Analysis", "description": "Q1 report" }`<br>**Resp:** `{ "id": "random-36-chars", "domainIdentifier": "...", ... }` |
| **GET** | `/` | 查询列表 | **Params:** `?maxResults=10&nextToken=...` |
| **GET** | `/{identifier}` | 获取详情 | **Path:** `domainIdentifier`, `identifier` (Project ID) |
| **DELETE** | `/{identifier}` | 删除 Project | **Resp:** 204 No Content |

---

## 💻 开发指南

### 项目结构
```text
src/main/java/com/example/
├── constant/       # 常量定义 (Regex, ID规则)
├── dto/            # API 请求体对象 (Request Body)
├── handler/        # Lambda 入口及路由分发
│   └── api/        # 具体业务逻辑 (Create/Get/List/Delete)
├── model/          # DynamoDB 数据模型 (@DynamoDbBean)
└── utils/          # 工具类 (ID生成, 分页Token)
```

### 关键逻辑
1.  **ID 生成**: 
    * Domain ID: `dzd[-_][Random(36)]`
    * Project ID: `[Random(36)]`
2.  **校验**: 
    * 所有 ID 和 ARN 均通过正则表达式严格校验。
    * Project 操作会校验其是否归属于路径中指定的 Domain。

