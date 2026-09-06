import dao.AlunoDAO;
import dao.CursoDAO;
import model.Aluno;
import model.Curso;
import com.github.javafaker.Faker;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando rotina com arquitetura POO + DAO...");

        try {
            // 1. Instancia os objetos de acesso aos dados (DAO)
            CursoDAO cursoDAO = new CursoDAO();
            AlunoDAO alunoDAO = new AlunoDAO();

            // 2. Define a lista de cursos e realiza o cadastro via CursoDAO
            List<String> nomesCursos = List.of(
                    "Engenharia de Software", "Sistemas de Informacao", "Ciencia da Computacao",
                    "Direito", "Administracao", "Psicologia", "Medicina",
                    "Enfermagem", "Agronomia", "Arquitetura"
            );

            List<Curso> cursos = cursoDAO.cadastrarCursos(nomesCursos);
            System.out.println("-> " + cursos.size() + " cursos cadastrados via CursoDAO!");

            // 3. Gera 5.000 objetos Aluno relacionando-os com os objetos Curso
            Faker faker = new Faker(new Locale("pt-BR"));
            Random random = new Random();
            List<Aluno> alunos = new ArrayList<>();

            for (int i = 0; i < 5000; i++) {
                String nomeAluno = faker.name().fullName();
                Curso cursoSorteado = cursos.get(random.nextInt(cursos.size()));

                alunos.add(new Aluno(nomeAluno, cursoSorteado));
            }

            alunoDAO.salvarEmLote(alunos);
            System.out.println("-> 5000 alunos gerados com Faker e salvos via AlunoDAO!");

        } catch (SQLException e) {
            System.err.println("Erro na execução: " + e.getMessage());
            e.printStackTrace();
        }
    }
}