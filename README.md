https://mvnrepository.com/artifact/com.github.javafaker/javafaker/1.0.2

  FRAMEWORK PARA OS NOMES FAKES

  MURIEL SSH ( O QUE UTILIZEI NA ULTIMA ATUALIZAÇÃO )
SHA256:COgV1M7/uBSVhaTWdOQ+mxrYZmvfJsUnnyPkr+TWlu4

GIT + INTELIJ + MAVEN + MAVEN REPOSITORY + PGADMIN

a definição das tabelas está versionada no arquivo `src/main/resources/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS cursos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS alunos (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    curso_id INT REFERENCES cursos(id) ON DELETE CASCADE
);
