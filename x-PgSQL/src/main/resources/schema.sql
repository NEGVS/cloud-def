CREATE TABLE IF NOT EXISTS users
(
    id BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    email VARCHAR(255)
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders
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
CREATE TABLE IF NOT EXISTS funds
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
CREATE TABLE IF NOT EXISTS payment_logs
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

-- 日志表
CREATE TABLE IF NOT EXISTS logs
(
    id BIGSERIAL PRIMARY KEY,
    remark     VARCHAR(255)  DEFAULT NULL,
    note       VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);