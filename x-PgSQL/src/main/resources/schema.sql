CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    email VARCHAR(255)
);

-- accounts 表：存储用户资金
CREATE TABLE accounts
(
    id BIGSERIAL PRIMARY KEY,
    user_id        BIGINT     NOT NULL UNIQUE,
    balance_bigint BIGINT     NOT NULL DEFAULT 0, -- 存最小货币单位（分）
    currency       VARCHAR(3) NOT NULL DEFAULT 'CNY',
    version        BIGINT     NOT NULL DEFAULT 0, -- 可用于乐观锁
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- orders 表：订单基本信息
CREATE TABLE orders
(
    id BIGSERIAL PRIMARY KEY,
    order_no      VARCHAR(64) NOT NULL UNIQUE,
    user_id       BIGINT      NOT NULL,
    amount_bigint BIGINT      NOT NULL, -- 金额（分）
    status        VARCHAR(32) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT now()
);
# 说明：金额使用整型（分）避免浮点误差；记录变动日志便于审计与补偿。
-- balance_changes 日志（可选）：审计/回滚/补偿用
CREATE TABLE balance_changes
(
    id BIGSERIAL PRIMARY KEY,
    account_id   BIGINT NOT NULL,
    order_id     BIGINT,
    delta_bigint BIGINT NOT NULL,
    reason       VARCHAR(128),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT now()
);
#
-- 订单表
CREATE TABLE orders
(
    id BIGSERIAL PRIMARY KEY,
    order_no   VARCHAR(64) UNIQUE NOT NULL,
    user_id    BIGINT             NOT NULL,
    amount     DECIMAL(10, 2)     NOT NULL,
    status     VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, FAILED
    version    INT         DEFAULT 1,         -- 乐观锁
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- 资金账户表
CREATE TABLE funds
(
    id BIGSERIAL PRIMARY KEY,
    user_id    BIGINT UNIQUE NOT NULL,
    balance    DECIMAL(10, 2) DEFAULT 0.00,
    version    INT            DEFAULT 1, -- 乐观锁
    created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
-- 扣款日志表
CREATE TABLE payment_logs
(
    id BIGSERIAL PRIMARY KEY,
    order_no      VARCHAR(64)    NOT NULL,
    user_id       BIGINT         NOT NULL,
    amount        DECIMAL(10, 2) NOT NULL,
    status        VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SUCCESS, FAILED, COMPENSATED
    error_message TEXT,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_payment_logs_order_no ON payment_logs (order_no);
CREATE INDEX idx_payment_logs_user_id ON payment_logs (user_id);
CREATE INDEX idx_payment_logs_status ON payment_logs (status);