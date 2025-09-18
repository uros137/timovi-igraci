-- Seed podaci za brzu demonstraciju
INSERT INTO team (id, name, city) VALUES
  (1, 'Partizan', 'Beograd'),
  (2, 'Crvena Zvezda', 'Beograd')
ON DUPLICATE KEY UPDATE name=VALUES(name), city=VALUES(city);

INSERT INTO player (id, full_name, age, position, team_id) VALUES
  (1, 'Milan Jovanović', 24, 'DF', 1),
  (2, 'Petar Petrović', 21, 'MF', 1),
  (3, 'Nikola Nikolić', 27, 'FW', 2)
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), age=VALUES(age), position=VALUES(position), team_id=VALUES(team_id);
