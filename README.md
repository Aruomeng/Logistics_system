# 智创农链：全链路智慧农产品供应链管理系统  
# Logistics & Smart Agricultural Supply Chain Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue.js-3.5-4fc08d.svg)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **智创农链 (Smart Agri-Link)** 是一款基于 **Spring Boot 3** 与 **Vue 3** 构建的企业级智慧农产品供应链管理系统。系统深度整合了全链路数字溯源、有限状态机(DFA)流程管控以及现代 BI 商业智能分析大屏，旨在解决传统农产品流转中信息不对称、冷链不可追溯及履约失控等核心痛点。

---

## 🚀 核心亮点 (Core Innovations)

### 1. 基于 DFA 的全链路履约状态机 (Finite State Machine)
系统引入了严密的**有向状态机模型**管控订单生命周期。从 `待接单` 到 `冷链监测中` 再到 `已完成`，每一步状态扭转都必须经过原子性操作验证，彻底杜绝了业务流程中的“状态飞线”与脏数据。

### 2. 生成式数字溯源引擎 (Traceability Passport)
采用 Google ZXing 驱动。农产品从产地入库起即分配唯一 `TraceCode`，系统自动压制 JSON 数据矩阵并生成**轻量级数字护照**。消费者无需登录，移动端直接扫码即可查询包含冷链温度记录、地理位置变迁在内的全景档案。

### 3. Bento Box 风格 BI 数据洞察中心 (Data Visualization)
放弃传统平庸的中后台表格展示，采用现代化的 **Bento Grid (便当盒)** 布局。通过深度定制的 ECharts 引擎，实时监控 GMV 走势、各品类动销率（Turnover Rate）以及高危冷链异常率，为平台决策提供毫秒级数据支撑。

### 4. 零信任与静默交互机制 (Professional DX)
- **Zero-Trust Validation**：对违规商品下架、用户封禁等敏感操作实施深层劫持，强制二次秘钥校验。
- **Silent Refresh Tokens**：内置双令牌(Dual-Token)静默续期逻辑，利用 Axios 响应拦截池保障用户在高负载操作时的身份验证无感顺延。

---

## 🛠️ 技术架构 (Architecture)

### 后端栈 (Backend Stack)
- **核心框架**：Spring Boot 3.2.5 (JDK 21)
- **持久层**：MyBatis-Plus 3.5.7 + MySQL 8.x
- **安全鉴权**：Spring Security + JWT (jjwt 0.12.5)
- **缓存/消息**：Redis 7.x
- **辅助工具**：Hutool, Lombok, EasyExcel (报表导出), ZXing (二维码生成)

### 前端栈 (Frontend Stack)
- **框架/构建**：Vue 3.5 (Composition API) + Vite 7
- **状态管理**：Pinia
- **UI 组件库**：Element Plus + Tailwind CSS
- **可视化**：ECharts 6.0
- **开发语言**：TypeScript 5.9

---

## 📂 项目结构 (Project Structure)

```text
Logistics
├── backend                 # 后端源码 (Maven 结构)
│   ├── src/main/java       # 核心业务逻辑 (Controller, Service, Security, DFA)
│   └── src/main/resources  # 配置文件与 SQL 初始化脚本 (init.sql, demand_tables.sql)
├── frontend                # 前端源码 (Vue + Vite + TS)
│   ├── src/views           # 角色化视图模块 (Admin, Supplier, Logistics, Consumer)
│   └── src/api             # 封装的 Axios API 调用池
└── 项目说明                 # 完整的系统详解、答辩 QA 库及核心创新点文档
```

---

## 📦 快速部署 (Quick Start)

### 1. 基础设施准备
- 安装 **Java 21**, **Node.js 18+**, **MySQL 8.0**, **Redis**.
- 创建数据库 `supply_chain`，依次执行 `backend/src/main/resources/sql/` 目录下的 `init.sql` 与 `demand_tables.sql`。

### 2. 后端启动
```bash
cd backend
# 修改 application.yml 中的数据库与 Redis 连接信息
mvn clean install
mvn spring-boot:run
```
> 后端 API 文档：`http://localhost:8080/api/v1/swagger-ui.html`

### 3. 前端启动
```bash
cd frontend
npm install
npm run dev
```
> 访问地址：`http://localhost:5173` (代理默认指向 8080)

---

## 👥 角色矩阵 (RBAC)

1. **管理员 (Administrator)**：全局治理、公告发布、供需审计、BI 全屏监控。
2. **供应方 (Supplier)**：SKU 管理、产地冷库存证、溯源码生成、订单履约。
3. **物流方 (Logistics)**：运单承接、冷链温度上报、中转站打卡。
4. **消费者 (Consumer)**：供应大厅浏览、求购需求发布、全生命周期溯源查询。

---

## 📝 证书 (License)
本项目遵循 [MIT License](LICENSE) 开源协议。

© 2026 智创农链开发团队. All Rights Reserved.
