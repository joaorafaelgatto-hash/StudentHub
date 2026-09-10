import dao.CursoDAO;
import dao.AlunoDAO;
import model.Curso;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        CursoDAO cursoDAO = new CursoDAO();
        AlunoDAO alunoDAO = new AlunoDAO();

        try (Connection conn = util.ConexaoBanco.getConnection()) {
            System.out.println("Processando...");
            cursoDAO.limparTabelas();

            List<String> nomesCursos = Arrays.asList("Ciencia da Computacao", "Engenharia de Software", "Sistemas de Informacao",
                    "Direito", "Administracao", "Medicina", "Agronomia",
                    "Psicologia", "Enfermagem", "Arquitetura"
            );

            List<Curso> cursos = cursoDAO.cadastrarCursos(nomesCursos);
            List<Integer> cursosIds = cursos.stream()
                    .map(Curso::getId)
                    .collect(Collectors.toList());

            alunoDAO.inserirAlunosComFaker(conn, cursosIds, 5000);
            cursoDAO.exibirRelatorio(conn);

        } catch (SQLException e) {
            System.err.println("Erro" + e.getMessage());
            e.printStackTrace();
        }
    }
}