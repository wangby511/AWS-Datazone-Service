# AWS Datazone 模拟服务 API

本项目是一个使用 Java 17、AWS CDK 和 DynamoDB Enhanced Client 构建的后端服务，旨在模拟 AWS Datazone 的 Domain 和 Project 资源管理 API。Lambda 函数通过 API Gateway 作为统一入口进行路由分发。

## 架构概览
* **基础设施即代码 (IaC)**: AWS CDK (Java)
* **后端运行时**: AWS Lambda (Java 17)
* **数据库**: Amazon DynamoDB (两个表：DomainTable 和 ProjectTable)
* **API**: Amazon API Gateway (RestApi)

## 快速开始

### 1. 先决条件
* Java Development Kit (JDK) 17+
* Apache Maven 3+
* Node.js (用于 CDK)
* AWS CDK CLI (`npm install -g aws-cdk`)
* AWS 账户配置 (配置了凭证和默认区域)

### 2. 构建项目
在项目根目录运行 Maven 命令以编译代码并创建 Lambda 的 Fat Jar 包：

```bash
mvn clean install
```

### 3. 运行单元测试
运行所有 JUnit 5/Mockito 单元测试：

```bash
mvn test
```

### 4. 部署到 AWS
确保您的 AWS 环境已引导 (bootstrapped)。

```bash
cdk deploy
```

部署完成后，CDK 将输出 API Gateway 的 URL (`ApiUrl`)。

## API 端点一览

| 资源 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **Domain** | POST | `/domains/createDomain` | 创建一个新的 Domain。 |
| | GET | `/domains/listDomains` | 获取 Domain 列表 (支持分页)。 |
| | GET | `/domains/getDomain/{domainId}` | 获取指定 Domain 详情。 |
| | DELETE | `/domains/deleteDomain/{domainId}` | 删除指定 Domain。 |
| **Project** | POST | `/domains/{domainIdentifier}/projects/createProject` | 在指定 Domain 下创建 Project。 |
| | GET | `/domains/{domainIdentifier}/projects/listProjects` | 获取指定 Domain 下的 Project 列表 (使用 GSI)。 |
| | GET | `/domains/{domainIdentifier}/projects/getProject/{projectId}` | 获取指定 Project 详情。 |
| | DELETE | `/domains/{domainIdentifier}/projects/deleteProject/{projectId}` | 删除指定 Project。 |

## 技术细节
* **ID 生成**: Domain ID 遵循 `dzd[_-]...` 模式；Project ID 为 36 位随机字符串。
* **分页**: 列表 API 通过 Base64 编码/解码 DynamoDB 的 `ExclusiveStartKey` 实现无状态分页。
* **Project 查找**: `ProjectTable` 使用 `domainIdentifier` 作为全局二级索引 (GSI)，以支持按 Domain 高效列出项目。
