-- Создание таблицы с машинами
CREATE TABLE machine (
    Id SERIAL PRIMARY KEY,
    Logo TEXT,
    Model TEXT,
    Cost MONEY
);
-- Создание таблицы с людьми и связка с машинами по айди
CREATE TABLE person (
    Id SERIAL primary KEY,
    Name TEXT,
    Age INTEGER,
    Prava BOOLEAN,
    Machine_Id SERIAL REFERENCES machine (Id)
);

