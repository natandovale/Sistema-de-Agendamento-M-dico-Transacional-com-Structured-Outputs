INSERT INTO doctors (id, name, specialty) VALUES 
('doc-1', 'Dr. Alício', 'Cardiologia'),
('doc-2', 'Dr. Roberto', 'Ortopedia'),
('doc-3', 'Dra. Beatriz Santos', 'Dermatologia')
ON CONFLICT (id) DO NOTHING;
