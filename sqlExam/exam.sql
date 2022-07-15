-- получить имена всех person, которые не состоят в компании с id = 5;
SELECT * FROM person WHERE company_id <> 5;

-- получить название компании для каждого человека.
SELECT p.name, c.name FROM person p
JOIN company c
ON c.id=p.company_id;

-- Получить название компании с максимальным количеством человек + количество человек в этой компании
SELECT c.name, count(*) cnt1
FROM company c
JOIN person p
ON p.company_id=c.id
GROUP BY c.name
HAVING count(*) = (
    SELECT max(cnt)
    FROM(
        SELECT count(p.company_id) cnt
        FROM person p
        GROUP BY p.company_id) t1);