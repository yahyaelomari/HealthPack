-- One database per service. Extensions (vector, btree_gist) belong in each
-- service's own Flyway migrations, not here, so the schema stays owned by the
-- service that uses it.
CREATE DATABASE identity;
CREATE DATABASE patient;
CREATE DATABASE scheduling;
CREATE DATABASE clinical;
CREATE DATABASE lab;
CREATE DATABASE pharmacy;
CREATE DATABASE billing;
CREATE DATABASE terminology;
CREATE DATABASE document;
CREATE DATABASE notification;
CREATE DATABASE audit;
CREATE DATABASE chat;
CREATE DATABASE ai;
