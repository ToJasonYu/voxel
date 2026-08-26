-- Seeded sample data the voice-to-SQL flow is allowed to query.
CREATE TABLE customers (
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL,
    signup_date DATE NOT NULL,
    plan_tier   TEXT NOT NULL
);

CREATE TABLE orders (
    id            SERIAL PRIMARY KEY,
    customer_id   INTEGER REFERENCES customers(id),
    order_date    DATE NOT NULL,
    total_amount  NUMERIC(10, 2) NOT NULL,
    status        TEXT NOT NULL
);

CREATE TABLE order_items (
    id           SERIAL PRIMARY KEY,
    order_id     INTEGER REFERENCES orders(id),
    product_name TEXT NOT NULL,
    quantity     INTEGER NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL
);

-- Application state: dashboards and the widgets voice commands add to them.
CREATE TABLE dashboard_sessions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dashboard_widgets (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID REFERENCES dashboard_sessions(id),
    chart_type TEXT NOT NULL,
    title      TEXT NOT NULL,
    sql_query  TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    position   INTEGER NOT NULL
);
