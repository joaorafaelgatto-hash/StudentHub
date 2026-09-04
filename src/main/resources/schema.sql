-- Criação do banco de dados (executado no PostgreSQL/pgAdmin)

CREATE TABLE IF NOT EXISTS cursos (
                                      id SERIAL PRIMARY KEY,
                                      nome VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS alunos (
                                      id SERIAL PRIMARY KEY,
                                      nome VARCHAR(100) NOT NULL,
    curso_id INT REFERENCES cursos(id) ON DELETE CASCADE
    );