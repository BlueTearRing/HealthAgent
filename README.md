# AI 健康平台

一个基于 `Spring Boot + Spring AI + Vue 3` 的智能健康应用项目，聚焦于 **AI 饮食规划** 与 **AI 医疗问答** 两个核心场景，并支持流式对话、会话记忆、知识库增强检索（RAG）以及工具调用扩展。

## 项目简介

本项目由前后端两个子系统组成：

- **后端**：提供用户管理、AI 对话接口、聊天记忆持久化、知识库增强、工具调用与接口文档。
- **前端**：提供登录注册、应用首页、AI 聊天页面、聊天会话列表与流式消息展示。

当前项目的产品形态是一个健康类 AI 平台原型，适合用于：

- 智能健康问答系统原型
- Spring AI / Agent 应用学习示例
- 毕业设计或课程项目基础框架
- RAG + Tool Calling + 多轮对话能力验证

## 核心功能

### 1. 用户系统

支持基础用户能力：

- 用户注册
- 用户登录
- 获取当前登录用户
- 用户退出登录
- Session 登录态管理

### 2. AI 饮食规划

提供饮食和营养相关的智能问答能力：

- 针对用户饮食需求进行咨询
- 基于知识库检索增强生成回答
- 支持多轮会话记忆
- 通过 `SSE` 流式返回 AI 响应

### 3. AI 医疗问答

提供医疗健康信息咨询能力：

- 回答常见健康与医疗相关问题
- 使用向量库/检索增强辅助回答
- 支持多轮上下文记忆
- 支持工具调用扩展
- 通过 `SSE` 流式返回 AI 响应

### 4. 自主规划智能体

后端已提供一个自主规划智能体入口：

- 支持流式输出
- 当前前端尚未接入对应页面

## 技术栈

### 后端

- Java 21
- Spring Boot 3.5.6
- MyBatis-Plus 3.5.7
- Spring AI Alibaba 1.0.0-M6.1
- Spring AI MCP Client 1.0.0-M6
- Spring AI Vector Store Advisors 1.1.0-M3
- MySQL
- Knife4j 4.4.0
- Hutool 5.8.26
- Lombok
- Reactor

### 前端

- Vue 3
- Vite 5
- Vue Router 4
- Element Plus 2
- Axios
- Pinia

## 项目结构

```text
healthy/
├─ agent-frontend/                 # Vue 3 前端项目
│  ├─ src/
│  │  ├─ api/                      # 前端接口封装
│  │  ├─ router/                   # 前端路由
│  │  ├─ stores/                   # 状态管理
│  │  ├─ utils/                    # 请求、SSE、Markdown 工具
│  │  └─ views/                    # 页面：首页/登录/注册/聊天页
│  └─ package.json
├─ sql/
│  └─ create_table.sql             # 数据库建表脚本
├─ src/
│  └─ main/
│     ├─ java/com/liagent/aiagent/
│     │  ├─ advisor/               # AI 顾问配置
│     │  ├─ agent/                 # 智能体实现
│     │  ├─ app/                   # AI 应用封装
│     │  ├─ common/                # 通用返回结构等
│     │  ├─ config/                # 配置类
│     │  ├─ controller/            # 接口控制器
│     │  ├─ entity/                # 实体类
│     │  ├─ mapper/                # MyBatis Mapper
│     │  ├─ memory/                # 聊天记忆持久化
│     │  ├─ service/               # 业务服务
│     │  └─ tool/                  # 工具调用实现
│     └─ resources/
│        └─ application.yml        # Spring Boot 配置
├─ pom.xml
└─ README.md
```

## 主要页面

前端已实现如下页面：

- `/home`：应用首页
- `/login`：登录页
- `/register`：注册页
- `/diet-plan`：AI 饮食规划聊天页
- `/medical-chat`：AI 医疗问答聊天页

## 后端接口概览

后端服务默认地址：

- `http://localhost:8123/api`

### 用户接口

- `POST /user/register`：用户注册
- `POST /user/login`：用户登录
- `POST /user/logout`：用户注销
- `GET /user/get/login`：获取当前登录用户

### AI 接口

- `GET /diet_plan_agent`：饮食规划 AI 流式响应
- `GET /medical_manage_agent`：医疗问答 AI 流式响应
- `GET /auto_plan_agent`：自主规划智能体流式响应

### 会话接口

前端代码中已存在以下接口调用：

- `POST /conversation/create/{userId}`
- `GET /conversation/list/{userId}/{aiType}`
- `DELETE /conversation/{userId}/{conversationId}`

但根据当前仓库代码，后端尚未发现对应的 `ConversationController / Service / Mapper / Entity` 实现，因此这一模块可能仍在开发中，或存在未提交代码。

## AI 能力设计

### 1. 多轮对话记忆

项目通过 `ChatMemoryByMysql` 将聊天记录持久化到 MySQL，实现：

- 会话消息存储
- 历史上下文读取
- 上下文清除

相关表：

- `chat_message`

### 2. 饮食规划 RAG

饮食规划模块使用 DashScope 云知识库检索：

- `DashScopeDocumentRetriever`
- `RetrievalAugmentationAdvisor`

说明该模块具备基于知识库的回答增强能力。

### 3. 医疗问答增强

医疗问答模块使用 `QuestionAnswerAdvisor(vectorStore)`，说明该模块具备向量检索增强问答能力。

### 4. 工具调用

项目已接入的工具包括：

- `WebSearchTool`
- `ImageSearchTool`

代码中还可见部分预留但未启用的工具：

- `PDFGenerationTool`
- `TerminateTool`
- 资源下载工具

### 5. MCP 扩展

项目已引入 `spring-ai-mcp-client-spring-boot-starter`，具备进一步扩展 MCP 工具能力的基础。

## 数据库设计

SQL 脚本位置：

- `sql/create_table.sql`

当前脚本包含以下表：

- `user`：用户表
- `chat_message`：聊天消息表
- `conversation`：会话表

其中：

- `user` 用于管理平台用户
- `chat_message` 用于保存 AI 与用户消息记录
- `conversation` 用于存储会话元信息