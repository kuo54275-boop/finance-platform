# 💰 数字化财务管理综合平台（Finance Platform）

> 个人记账与财务管理平台 —— 数字化实训系统：收支记账联动资产账户、账单到期提醒、预算管控、
> Excel 账单导出与数据备份，配合系统统计看板与定时任务，模拟真实财务场景下的完整记账闭环。

## 📌 项目简介

前后端分离的个人财务管理平台。用户注册登录后绑定自己的资产账户（现金 / 银行卡 / 支付宝 / 微信），记一笔收支时账户余额自动联动，看板统计收支构成与预算执行进度，信用卡还款、房租等账单到期自动提醒，数据支持 Excel 导出与 JSON 全量备份。

| 业务闭环 | 流程 |
|----------|------|
| 记账闭环 | 注册登录 → 绑定资产账户 → 记一笔（余额联动） → 看板统计 / 预算进度 |
| 提醒闭环 | 添加账单提醒（还款日/到期日） → 到期检查 → 已提醒标记 |
| 数据闭环 | 记账数据 → Excel 全量导出（自定义货币符号） / JSON 离线备份 |

## 🧩 功能模块

| 模块 | 主要功能 |
|------|----------|
| 用户中心 | 手机号 + 邮箱注册、登录、修改密码（**必须验证原密码**） |
| 收支记账 | 添加 / 修改 / 删除账目，**关联资产账户联动余额**，收支分类统计看板 |
| 资产账户 | 绑定现金 / 银行卡 / 支付宝 / 微信账户，余额实时联动 |
| 预算管理 | 按月按类别设置预算额度，看板展示执行进度 |
| 账单提醒 | 还款提醒添加 / 到期检查 / 删除（信用卡、房租等） |
| 系统管理（admin） | 全局宏观统计看板、全量用户列表、注销用户、预设收支类别管理 |
| 数据工具 | **EasyExcel 全量账单导出**（支持自定义货币符号）、JSON 全量数据备份 |
| 定时任务 | 每月 2 日自动"发工资"入账、每月 3 日自动"交房租"扣款、演示调试任务 |

## 🛠 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.1.5（Java 17）、MyBatis-Plus 3.5.5、SQLite（sqlite-jdbc 3.45.1.0）、EasyExcel 3.3.3（Excel 导出）、Lombok |
| 前端 | 单文件 Vue 2（CDN 引入，`index.html` 共 1000+ 行），直接 fetch 调用后端接口 |
| 架构 | 前后端分离；后端 `@CrossOrigin` 放行跨域；`@EnableScheduling` 开启定时任务 |

## 📂 项目结构

```
finance-platform/
├── index.html                  前端单文件页面（浏览器直接打开）
├── .gitignore
└── finance-platform/           Spring Boot 后端（IDEA 打开此文件夹）
    ├── pom.xml
    ├── api-test.http           接口测试文件（IDEA 中可直接运行测试）
    └── src/main/
        ├── java/com/finance/
        │   ├── controller/     接口入口（user / record / account / budget / bill / admin）
        │   ├── service/        业务逻辑
        │   ├── mapper/         MyBatis-Plus 数据访问层
        │   ├── entity/         实体类（user / record / account / category / budget / bill_notice）
        │   └── task/           定时任务（工资 / 房租 / 演示）
        └── resources/
            ├── application.yml 配置文件（⚠ 数据库路径需按本机修改）
            └── db/schema.sql   建表脚本（6 张表，幂等可重复执行）
```

## ⚙ 环境要求

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | 后端运行环境 |
| Maven | 3.6+ | 后端依赖管理（项目未内置 Maven Wrapper） |
| IntelliJ IDEA | 任意新版 | 打开 `finance-platform` 文件夹进行后端开发 |
| SQLiteStudio（可选） | 任意新版 | 可视化建表 / 查看数据 |

> 数据库无需安装任何服务：SQLite 就是一个文件，开箱即用。

## 🚀 快速启动

### 1. 创建数据库

**① 修改数据库路径**：编辑 `finance-platform/src/main/resources/application.yml`，将 `url` 改为本机路径，例如：

```yaml
spring:
  datasource:
    url: jdbc:sqlite:D:/你的路径/finance.db
```

**② 建表**：用 SQLiteStudio 打开数据库文件，执行 `src/main/resources/db/schema.sql`（或命令行 `sqlite3 finance.db < db/schema.sql`）。

### 2. 启动后端（端口 8080）

```bash
cd finance-platform
mvn spring-boot:run
```

### 3. 打开前端

直接双击根目录 `index.html`（或 VS Code Live Server 打开）。

### 4. 注册账号开始使用

打开页面后先"注册"（手机号 + 邮箱 + 密码），登录后即可记账。

## 📡 接口一览

| 模块 | 接口 |
|------|------|
| 用户 | `POST /user/register` 注册、`POST /user/login` 登录 |
| 记账 | `POST /record/add`、`GET /record/list`、`GET /record/dashboard`、`PUT /record/update`、`DELETE /record/delete/{id}` |
| 数据工具 | `GET /record/export` Excel 导出、`GET /record/backup` JSON 备份、`PUT /record/change-password` 改密码（验证原密码） |
| 资产账户 | `GET /account/list`、`POST /account/add`、`DELETE /account/delete` |
| 预算 | `POST /budget/set`、`GET /budget/progress` |
| 账单提醒 | `POST /bill/add`、`GET /bill/list`、`GET /bill/check-remind`、`DELETE /bill/delete/{id}` |
| 系统管理 | `GET /admin/sys-stats` 统计看板、`GET /admin/users`、`DELETE /admin/user/delete/{id}`、`GET/POST/DELETE /admin/category/*` 类别管理 |

> 完整请求示例见 `finance-platform/api-test.http`（IDEA 可直接运行）。

## ❓ 常见问题

| 问题 | 解决 |
|------|------|
| 启动报"数据库文件找不到/无法打开" | 检查 `application.yml` 中的 `url` 路径是否为本机存在的路径 |
| 报"no such table: xxx" | 数据库里还没建表：执行 `db/schema.sql` 建表脚本 |
| 前端页面调用接口失败 | 确认后端 8080 已启动；跨域已由后端 `@CrossOrigin` 放行 |
| 后端启动报"Port 8080 was already in use" | `netstat -ano \| findstr :8080` 找到 PID 后 `taskkill /PID <pid> /F` |
| 想换回 MySQL | 将 `pom.xml` 中 `sqlite-jdbc` 替换为 `mysql-connector-j`，修改 `application.yml` 数据源配置即可 |

## 📄 其他

- 数据库文件（`finance.db`）为本地数据，未纳入版本控制（已 gitignore），新环境按上文建库即可
- 远程仓库：https://github.com/kuo54275-boop/finance-platform
