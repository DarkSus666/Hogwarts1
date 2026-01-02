-- Получить всех студентов, возраст которых находится между 10 и 20 (можно
-- подставить любые числа, главное, чтобы нижняя граница была меньше верхней).
select * from student where age > 18 and age < 30;

-- Получить всех студентов, но отобразить только список их имен.
select name from student;

-- Получить всех студентов, у которых в имени присутствует буква О (или любая другая).
select * from student where name like '%u%';

-- Получить всех студентов, у которых возраст меньше идентификатора.
select * from student where age < id;

-- Получить всех студентов упорядоченных по возрасту.
select * from student order by age;

select * from student as s, faculty as f where s.faculty_id = f.id