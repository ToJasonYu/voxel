-- Second layer of defense: even if the app-level validator in SqlValidator.kt had
-- a bug, queries run under this role physically cannot write anything or see
-- tables outside the allowlist, because Postgres itself won't let them.
CREATE ROLE voxel_readonly LOGIN PASSWORD 'voxel_readonly';

GRANT CONNECT ON DATABASE voxel TO voxel_readonly;
GRANT USAGE ON SCHEMA public TO voxel_readonly;
GRANT SELECT ON customers, orders, order_items TO voxel_readonly;
-- No grants at all on dashboard_sessions / dashboard_widgets: this role never touches app state.
