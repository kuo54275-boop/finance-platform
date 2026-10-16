-- ============================================================
-- 数字化财务管理综合平台 - 建表脚本（SQLite）
-- 使用方式（任选其一）：
--   1. SQLiteStudio 打开数据库文件后执行本脚本
--   2. 命令行：sqlite3 finance.db < db/schema.sql
-- 幂等设计：可重复执行（IF NOT EXISTS，已存在的表会跳过）
-- ============================================================

-- 用户表：手机号/邮箱注册登录，role 区分普通用户(user)与超级管理员(admin)
CREATE TABLE IF NOT EXISTS user (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,           -- 用户ID，自增
    phone       TEXT,                                        -- 手机号（登录账号）
    email       TEXT,                                        -- 邮箱
    password    TEXT NOT NULL,                               -- 密码
    nickname    TEXT,                                        -- 昵称
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,          -- 注册时间
    role        TEXT DEFAULT 'user'                          -- 角色：user普通用户 / admin超级管理员
);

-- 资产账户表：现金/银行卡/支付宝/微信，记一笔时联动余额
CREATE TABLE IF NOT EXISTS account (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,               -- 账户ID，自增
    user_id INTEGER NOT NULL,                                -- 所属用户
    name    TEXT NOT NULL,                                   -- 账户名称（如：招商银行卡）
    type    TEXT NOT NULL,                                   -- 类型：CASH现金 / CARD银行卡 / ALIPAY支付宝 / WECHAT微信
    balance REAL DEFAULT 0.0                                 -- 账户余额
);

-- 收支账目表：记一笔（支出/收入），关联资产账户时联动扣减或增加余额
CREATE TABLE IF NOT EXISTS record (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,           -- 账目ID，自增
    user_id     INTEGER NOT NULL,                            -- 所属用户
    type        TEXT NOT NULL,                               -- 类型：EXPENSE支出 / INCOME收入
    category    TEXT,                                        -- 分类：餐饮、交通、工资等
    amount      REAL NOT NULL,                               -- 金额
    remark      TEXT,                                        -- 备注说明
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,          -- 记账时间
    account_id  INTEGER                                      -- 关联资产账户ID（可不关联）
);

-- 收支类别表：系统预设类别 + 用户自定义类别
CREATE TABLE IF NOT EXISTS category (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,             -- 类别ID，自增
    name      TEXT NOT NULL,                                 -- 类别名称（如：餐饮、交通、工资）
    type      TEXT NOT NULL,                                 -- 类型：EXPENSE支出 / INCOME收入
    is_preset INTEGER DEFAULT 1                              -- 是否系统预设：1是 / 0用户自定义
);

-- 预算表：按月按类别设置预算额度，看板展示执行进度
CREATE TABLE IF NOT EXISTS budget (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,              -- 预算ID，自增
    user_id  INTEGER NOT NULL,                               -- 所属用户
    category TEXT NOT NULL,                                  -- 预算类别（如：餐饮、购物）
    amount   REAL NOT NULL,                                  -- 预算额度
    month    TEXT NOT NULL                                   -- 预算月份（如：2026-07）
);

-- 账单提醒表：信用卡还款/房租等到期账单提醒
CREATE TABLE IF NOT EXISTS bill_notice (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,           -- 提醒ID，自增
    user_id     INTEGER NOT NULL,                            -- 所属用户
    title       TEXT NOT NULL,                               -- 账单标题（如：房租、信用卡还款）
    amount      REAL NOT NULL,                               -- 账单金额
    due_date    TEXT NOT NULL,                               -- 到期日期（如：2026-07-15）
    is_reminded INTEGER DEFAULT 0                            -- 是否已提醒：0未提醒 / 1已提醒
);
