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

--Связка через many to many (для общего развития)
-- 1. Таблица с машинами (остается без изменений)
CREATE TABLE machine (
    Id SERIAL PRIMARY KEY,
    Logo TEXT,
    Model TEXT,
    Cost MONEY
);

-- 2. Таблица с людьми (убираем ссылку на машину)
CREATE TABLE person (
    Id SERIAL PRIMARY KEY,
    Name TEXT,
    Age INTEGER,
    Prava BOOLEAN
    -- Столбец Machine_Id удален!
);

-- 3. Таблица-связка для отношения Many-to-Many
CREATE TABLE person_machine (
    Person_Id INTEGER NOT NULL,
    Machine_Id INTEGER NOT NULL,
    -- Дополнительные атрибуты отношения (опционально)
    Purchase_Date DATE,       -- Когда человек купил эту машину
    Is_Primary_Owner BOOLEAN, -- Является ли основным владельцем

    -- Составной первичный ключ (предотвращает дублирование связей)
    PRIMARY KEY (Person_Id, Machine_Id),

    -- Внешние ключи
    FOREIGN KEY (Person_Id) REFERENCES person(Id) ON DELETE CASCADE,
    FOREIGN KEY (Machine_Id) REFERENCES machine(Id) ON DELETE CASCADE
);
