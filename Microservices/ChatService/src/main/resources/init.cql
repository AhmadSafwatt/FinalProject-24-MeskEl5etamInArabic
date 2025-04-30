CREATE KEYSPACE IF NOT EXISTS chat_keyspace
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};