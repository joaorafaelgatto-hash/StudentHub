package dao;
import model.Curso;
import util.ConexaoBanco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {
public List<Curso> cadastrarCursos(List<String> nomesCursos) throws SQLException {

List<Curso> cursosCadastrados = new ArrayList<>();
String sql = "INSERT INTO cursos (nome) VALUES (?) RETURNING id;";

try (Connection conn = ConexaoBanco.getConnection()) {
    for (String nome : nomesCursos) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                cursosCadastrados.add(new Curso(id, nome));
            }
        }
    }
}
return cursosCadastrados;
    }
    public static void exibirRelatorio(Connection conn) throws SQLException {
        String sql = """
            SELECT c.nome AS curso, COUNT(a.id) AS total_alunos
            FROM cursos c
            LEFT JOIN alunos a ON c.id = a.curso_id
            GROUP BY c.nome
            ORDER BY total_alunos DESC;
        """;

        System.out.println("\n--- RELATORIO: ALUNOS POR CURSO ---");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("%-30s | %d alunos\n", rs.getString("curso"), rs.getInt("total_alunos"));
            }
        }
        System.out.println("-----------------------------------");
    }
    public void limparTabelas() throws SQLException {
        String sql = "TRUNCATE TABLE alunos, cursos RESTART IDENTITY CASCADE;";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
            System.out.println("-> Tabelas 'alunos' e 'cursos' limpadas com sucesso!");
        }
    }
}
