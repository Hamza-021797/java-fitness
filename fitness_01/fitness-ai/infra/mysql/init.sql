CREATE DATABASE IF NOT EXISTS fitness_user;
CREATE DATABASE IF NOT EXISTS fitness_activity;
CREATE DATABASE IF NOT EXISTS fitness_ai;
CREATE USER IF NOT EXISTS 'fitness'@'%' IDENTIFIED BY 'fitness';
GRANT ALL PRIVILEGES ON fitness_user.* TO 'fitness'@'%';
GRANT ALL PRIVILEGES ON fitness_activity.* TO 'fitness'@'%';
GRANT ALL PRIVILEGES ON fitness_ai.* TO 'fitness'@'%';
FLUSH PRIVILEGES;
