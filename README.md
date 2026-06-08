# TestPilot AI

> AI 驱动测试工程提效工具
> 支持 PRD 多版本知识库、RAG 检索、测试用例生成、用例质量评估、语义去重、自动化脚本生成与执行、AI 应用专项测试和 Agent 工具编排。

---

## 一、项目简介

TestPilot AI 是一个面向测试工程师和测试开发工程师的 AI 测试工具平台，目标是将大模型、RAG、Embedding、Agent、自动化测试等能力融入日常测试流程，提升需求理解、测试设计、测试资产沉淀和自动化执行效率。

平台围绕完整测试链路进行设计：

```text
PRD 上传
→ 产品知识库构建
→ RAG 检索
→ 测试用例生成
→ 用例质量评审
→ 语义去重
→ 用例集管理
→ 版本对比
→ 自动化脚本生成
→ 自动化执行
→ AI 应用专项测试
→ Agent Planner 编排执行
```

适用场景包括：

* 测试工程师根据 PRD 快速生成测试点和测试用例
* 根据历史版本知识库辅助新需求测试设计
* 按项目、版本、功能模块检索产品知识
* 评估 RAG 召回质量和知识库构建质量
* 对 AI 生成测试用例进行质量评分和语义去重
* 将测试用例生成 pytest + requests 自动化脚本
* 执行自动化脚本并解析 JUnit XML 报告
* 对 AI 应用进行 Prompt 注入、幻觉、知识越权等专项测试
* 使用 Agent Planner 编排知识库评估、RAG 评测、自动化执行等测试工具

---

## 二、核心能力

### 1. PRD 多版本知识库

支持测试工程师上传不同版本的 PRD 文档，并按照项目、版本号、功能模块构建产品知识库。

能力包括：

* PRD 文档上传
* 文档内容解析
* Parent / Child Chunk 切片
* Embedding 向量化
* pgvector 向量存储
* 按项目、版本、模块进行语义检索
* 支持新需求结合旧版本知识库生成测试用例

---

### 2. RAG 检索与评测

平台不仅支持 RAG 检索，还提供 RAG 评测闭环。

支持指标：

* Recall@K
* MRR
* 来源命中率
* 平均得分
* 标准问题集管理
* 每题召回上下文分析
* 期望关键词、期望来源、期望模块、期望版本校验

用于判断知识库是否真正“可用、可召回、可解释”。

---

### 3. 测试用例生成

支持基于以下输入生成测试用例：

* 自然语言需求
* PRD 知识库上下文
* 历史版本产品知识
* 功能模块信息
* 测试设计 Skill / Prompt 模板

生成维度包括：

* 正常流程
* 异常流程
* 边界值
* 参数校验
* 数据一致性
* 权限校验
* 安全性
* 兼容性
* 弱网 / 并发 / 幂等
* 自动化建议

---

### 4. 用例质量评审与语义去重

针对 AI 生成用例可能出现的泛化、重复、覆盖不足等问题，平台提供：

* 用例质量评分
* 缺失场景补全
* 人工采纳 / 驳回
* 用例集沉淀
* Embedding + pgvector 语义去重
* 同任务、同版本、同项目、跨版本去重

---

### 5. 用例集管理与版本对比

支持将审核通过的测试用例沉淀为正式测试资产，并进行版本化管理。

能力包括：

* 创建用例集
* 添加 / 移除用例
* 用例集详情查看
* 新旧版本用例集对比
* 新增 / 删除 / 修改 / 未变化用例分析
* Excel 导出

---

### 6. 自动化脚本生成与执行

平台支持将测试用例转换为接口自动化脚本。

当前支持：

* pytest + requests 脚本生成
* 生成 requirements.txt、pytest.ini、conftest.py、测试脚本
* 脚本 zip 下载
* 后端执行 pytest
* 解析 JUnit XML
* 统计通过、失败、错误、跳过数量
* 保存 stdout、stderr、执行日志和单用例结果

---

### 7. AI 应用专项测试

平台内置 AI 应用测试数据集管理与执行器，用于测试大模型应用质量。

覆盖场景：

* RAG 准确性
* RAG 来源引用
* Prompt 注入
* 幻觉
* 知识越权
* 输出格式稳定性
* Agent 工具调用
* 拒答能力
* 一致性测试

支持指标：

* 平均分
* 准确性通过率
* 安全通过率
* 格式通过率
* 平均响应耗时
* Prompt 注入成功数
* 幻觉风险数
* 知识泄露风险数

---

### 8. Agent Planner / Tool Calling

平台设计了半自动 Agent 编排能力。

核心思路：

```text
用户目标
→ LLM 生成执行计划
→ 后端工具白名单校验
→ 用户确认执行
→ 后端按步骤调用工具
→ 保存每一步执行结果
```

内置工具包括：

* 知识库检索
* 知识库质量评估
* RAG 评测执行
* 测试用例语义去重
* 用例集版本对比
* 自动化脚本生成
* 自动化脚本执行
* AI 应用测试执行

这种方式避免让大模型自由操作系统，降低 Agent 执行风险，更适合企业级落地。

---

## 三、技术栈

### 后端

* Java 21
* Spring Boot
* MyBatis-Plus
* PostgreSQL
* pgvector
* Redis
* MinIO
* Docker / Docker Compose
* Spring AI
* DashScope / Qwen
* OpenAI Compatible API
* Apache POI
* PDFBox
* EasyExcel

### AI 能力

* LLM
* RAG
* Embedding
* Prompt Engineering
* Agent Planner
* Tool Calling
* AI 应用测试

### 自动化测试

* Python
* pytest
* requests
* JUnit XML

### 前端

* Vite
* React
* TypeScript
* Ant Design
* React Router
* Axios
* TanStack Query
* Zustand
* ECharts

---

## 四、系统架构

```text
testpilot-ai
├── 前端控制台
│   ├── 项目管理
│   ├── PRD 文档管理
│   ├── 知识库检索
│   ├── RAG 评测
│   ├── 测试用例管理
│   ├── 自动化测试
│   ├── AI 应用测试
│   └── Agent Planner
│
├── 后端服务
│   ├── 项目与文档模块
│   ├── 知识库构建模块
│   ├── RAG 检索模块
│   ├── 测试用例生成模块
│   ├── 用例质量评估模块
│   ├── 用例集与版本对比模块
│   ├── 自动化脚本生成模块
│   ├── 自动化执行模块
│   ├── AI Eval 模块
│   └── Agent Planner 模块
│
├── 基础设施
│   ├── PostgreSQL + pgvector
│   ├── Redis
│   ├── MinIO
│   └── Docker Compose
│
└── AI 服务
    ├── DashScope / Qwen Chat Model
    ├── DashScope Embedding Model
    └── Spring AI / OpenAI Compatible API
```

---

## 五、环境准备

### 1. 基础环境

请先安装：

* JDK 21
* Maven 3.9+
* Docker
* Docker Compose
* Node.js 18+
* Python 3.10+

---

### 2. 启动基础服务

项目使用 PostgreSQL + pgvector、Redis、MinIO 作为基础依赖。

```bash
docker compose up -d
```

检查服务：

```bash
docker ps
```

默认服务端口：

| 服务            | 地址                    |
| ------------- | --------------------- |
| PostgreSQL    | localhost:5432        |
| Redis         | localhost:6379        |
| MinIO API     | http://localhost:9000 |
| MinIO Console | http://localhost:9001 |

---

### 3. 初始化数据库

进入项目根目录后，依次执行 SQL 初始化脚本。

示例：

```bash
docker exec -i testpilot-postgres psql -U testpilot -d testpilot_ai < docker/postgres/init.sql
```

如果项目中存在分步骤 SQL，例如：

```text
docker/postgres/step21_xxx.sql
docker/postgres/step22_xxx.sql
docker/postgres/step23_xxx.sql
...
```

请按步骤顺序执行。

---

## 六、后端启动

### 1. 配置环境变量

如果使用真实 DashScope / Qwen 模型，需要配置：

```bash
export DASHSCOPE_API_KEY=你的阿里云百炼APIKey
```

如果使用 IntelliJ IDEA 启动，需要在：

```text
Run
→ Edit Configurations
→ Environment variables
```

中添加：

```text
DASHSCOPE_API_KEY=你的阿里云百炼APIKey
```

---

### 2. application.yml 示例

```yaml
server:
  port: 8080

spring:
  application:
    name: testpilot-ai-backend

  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/testpilot_ai
    username: testpilot
    password: testpilot123

  data:
    redis:
      host: localhost
      port: 6379
      database: 0

  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 60MB

storage:
  minio:
    endpoint: http://localhost:9000
    access-key: testpilot
    secret-key: testpilot123
    bucket-name: testpilot-prd

ai:
  embedding:
    provider: dashscope
    dimension: 1024

    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      endpoint: https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings
      model-name: text-embedding-v4
      timeout-seconds: 60

  chat:
    provider: dashscope

    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      endpoint: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
      model-name: qwen-plus
      timeout-seconds: 120
      temperature: 0.2
```

如果暂时不接真实模型，可以切换为 mock：

```yaml
ai:
  embedding:
    provider: mock
    dimension: 1024

  chat:
    provider: mock
```

---

### 3. 启动后端

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/swagger-ui.html
```

---

## 七、前端启动

进入前端项目目录：

```bash
cd testpilot-ai-web
```

安装依赖：

```bash
npm install
```

配置 `.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8080
```

启动：

```bash
npm run dev
```

默认访问：

```text
http://localhost:5173
```

---

## 八、主要接口模块

### 项目与知识库

| 模块     | 能力                            |
| ------ | ----------------------------- |
| 项目管理   | 创建项目、查询项目、维护项目信息              |
| PRD 管理 | 上传 PRD、解析文档、按版本管理             |
| 知识库构建  | 文档切片、Embedding、向量入库           |
| 知识库检索  | 按项目、版本、模块进行 RAG 检索            |
| 知识库评估  | 评估 Chunk、Embedding、模块、版本、召回质量 |

---

### RAG 评测

| 模块     | 能力                    |
| ------ | --------------------- |
| 评测集管理  | 创建 RAG 标准问题集          |
| 标准问题管理 | 配置标准答案、期望关键词、期望来源     |
| 评测运行   | 计算 Recall@K、MRR、来源命中率 |

---

### 测试用例

| 模块       | 能力                          |
| -------- | --------------------------- |
| 用例生成     | 基于需求和知识库生成测试用例              |
| 用例审核     | 人工采纳、驳回、备注                  |
| 质量评估     | 评分、问题识别、优化建议                |
| 语义去重     | Embedding + pgvector 检测重复用例 |
| 用例集      | 沉淀正式测试资产                    |
| 版本对比     | 对比不同版本用例差异                  |
| Excel 导出 | 导出用例集和对比报告                  |

---

### 自动化测试

| 模块   | 能力                         |
| ---- | -------------------------- |
| 脚本生成 | 生成 pytest + requests 自动化脚本 |
| 脚本下载 | 下载 zip 包                   |
| 脚本执行 | 后端执行 pytest                |
| 报告解析 | 解析 JUnit XML               |
| 执行结果 | 统计通过、失败、错误、跳过              |

---

### AI 应用测试

| 模块    | 能力                     |
| ----- | ---------------------- |
| 数据集管理 | 维护 AI 应用测试样本           |
| 应用配置  | 配置待测 AI 应用接口           |
| 测试执行  | 批量请求待测 AI 应用           |
| 规则评估  | 检查关键词、禁止词、格式、工具调用、来源   |
| 风险统计  | Prompt 注入、幻觉、知识越权等风险统计 |

---

### Agent Planner

| 模块   | 能力               |
| ---- | ---------------- |
| 工具注册 | 后端维护工具白名单        |
| 计划生成 | LLM 根据用户目标生成执行计划 |
| 人工确认 | 执行前进行人工确认        |
| 工具执行 | 后端按步骤调用工具        |
| 步骤追踪 | 保存每一步输入、输出、状态、错误 |
| 失败重试 | 支持失败步骤重试         |

---

## 九、典型使用流程

### 1. 构建产品知识库

```text
创建项目
→ 上传 PRD
→ 填写版本号和模块编码
→ 执行知识库构建
→ 查看 Chunk 和 Embedding 状态
```

### 2. 生成测试用例

```text
选择项目 / 版本 / 模块
→ 输入新需求
→ 检索相关知识库上下文
→ 调用 LLM 生成测试用例
→ 人工审核采纳
→ 加入用例集
```

### 3. 新版本回归分析

```text
上传新版本 PRD
→ 构建新版本知识库
→ 结合旧版本知识库生成增量用例
→ 与旧版本用例集进行对比
→ 输出新增 / 删除 / 修改 / 未变化用例
```

### 4. 自动化测试

```text
选择用例集
→ 生成 pytest + requests 脚本
→ 下载或后端执行
→ 解析 JUnit XML
→ 查看执行报告
```

### 5. AI 应用测试

```text
创建 AI Eval 数据集
→ 初始化 Prompt 注入 / 幻觉 / 越权样本
→ 配置待测 AI 应用接口
→ 执行测试
→ 查看准确性、安全性、格式稳定性指标
```

### 6. Agent 编排执行

```text
输入目标
→ Agent Planner 生成计划
→ 后端校验工具白名单
→ 用户确认
→ 后端执行工具链
→ 查看每一步执行结果
```

---

## 十、项目亮点

1. **完整测试工程闭环**
   覆盖从 PRD 到知识库、RAG、用例生成、自动化执行、AI 应用测试的完整流程。

2. **支持版本化知识库**
   可按项目、版本、功能模块构建知识库，支持新需求结合旧版本知识辅助测试设计。

3. **引入 RAG 评测体系**
   不仅支持检索，还通过 Recall@K、MRR、来源命中率等指标评估召回质量。

4. **提升 AI 生成用例可用性**
   通过质量评分、人工审核、缺失补全、语义去重等机制降低 AI 输出不可控风险。

5. **支持 AI 应用专项测试**
   覆盖 Prompt 注入、幻觉、知识越权、来源引用、输出格式、Agent 工具调用等大模型应用测试场景。

6. **Agent 安全可控**
   采用“LLM 规划 + 后端白名单工具执行 + 人工确认”的方式，降低 Agent 自主执行风险。

7. **工程化落地能力强**
   使用 Spring Boot、PostgreSQL、pgvector、Redis、MinIO、Docker、React 等主流技术栈，适合企业级扩展。

---

## 十一、开发计划

### 已规划能力

* 项目管理
* PRD 上传
* 知识库构建
* RAG 检索
* RAG 评测
* 测试用例生成
* 用例质量评估
* 用例语义去重
* 用例集管理
* 版本对比
* Excel 导出
* 自动化脚本生成
* 自动化脚本执行
* AI 应用测试数据集
* AI 应用测试执行器
* Agent Planner

### 后续可扩展方向

* Docker 沙箱执行自动化脚本
* Playwright UI 自动化脚本生成
* 接口文档 OpenAPI 解析
* UI 原型图识别生成测试用例
* 多模型路由与成本统计
* 测试覆盖率分析
* 缺陷预测与风险评估
* 企业权限体系
* 团队协作与审计日志
* CI/CD 集成
* GitHub Actions / Jenkins 集成

---

## 十二、常见问题

### 1. 后端启动时报 OpenAI API key must be set

说明 Spring AI OpenAI 自动配置启动了，但没有读取到 API Key。

解决方式：

```bash
export DASHSCOPE_API_KEY=你的Key
```

或者在 IDEA 启动配置中添加环境变量：

```text
DASHSCOPE_API_KEY=你的Key
```

如果暂时不想接真实模型，可以切换为 mock provider。

---

### 2. 前端请求后端跨域

后端需要配置全局 CORS。

本地开发时允许：

```text
http://localhost:5173
http://127.0.0.1:5173
```

---

### 3. pgvector 表创建失败

确认 PostgreSQL 镜像使用的是：

```yaml
image: pgvector/pgvector:pg16
```

并执行：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

---

### 4. Embedding 维度不匹配

当前默认维度为：

```text
1024
```

如果更换 Embedding 模型，需要同步修改：

* application.yml 中的 ai.embedding.dimension
* PostgreSQL 表中 vector(1024) 的维度
* 相关校验逻辑

---

### 5. 自动化执行失败

检查：

* 本机是否安装 Python
* requirements.txt 是否正确
* pytest 是否安装成功
* 后端进程是否有创建目录和执行脚本权限
* 待测接口 baseUrl 是否可访问

---

## 十三、免责声明

本项目用于 AI 测试工程提效方向的技术探索和工程实践。
AI 生成的测试用例、自动化脚本和评估结果需要测试工程师进行人工审核后再应用于正式测试流程。

---
