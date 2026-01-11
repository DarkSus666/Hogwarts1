-- получить информацию обо всех студентах школы Хогвартс вместе с названиями факультетов
select s.name, s.age, f.name from student s inner join faculty f on s.faculty_id = f.id;

-- получить только тех студентов, у которых есть аватарки
select a.student_id, s.name, s.age from student s inner join avatar a on s.id = a.student_id ;
