import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/URI";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin"; // senha do pgAdmin

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("-> Conectado ao PostgreSQL com sucesso!");


            List<Integer> cursosIds = cadastrarCursos(conn);
            inserirAlunosComFaker(conn, cursosIds, 5000);


            exibirRelatorio(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> cadastrarCursos(Connection conn) throws SQLException {
        String[] cursos = {
                "Ciencia da Computacao", "Engenharia de Software", "Sistemas de Informacao",
                "Direito", "Administracao", "Medicina", "Agronomia",
                "Psicologia", "Enfermagem", "Arquitetura"
        };

        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO cursos (nome) VALUES (?) RETURNING id;";

        for (String curso : cursos) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, curso);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
        }
        System.out.println("-> 10 cursos cadastrados.");
        return ids;
    }

    private static void inserirAlunosComFaker(Connection conn, List<Integer> cursosIds, int quantidade) throws SQLException {
        // Inicializa o Framework Faker em Portugues do Brasil
        Faker faker = new Faker(new Locale("pt-BR"));
        Random random = new Random();

        String sql = "INSERT INTO alunos (nome, curso_id) VALUES (?, ?);";
        conn.setAutoCommit(false); // Transacao em lote

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= quantidade; i++) {
                // Nome gerado realisticamente pelo framework
                String nomeCompleto = faker.name().fullName();
                int cursoSorteado = cursosIds.get(random.nextInt(cursosIds.size()));

                stmt.setString(1, nomeCompleto);
                stmt.setInt(2, cursoSorteado);
                stmt.addBatch();

                if (i % 1000 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
            conn.commit();
            System.out.println("-> " + quantidade + " alunos gerados com Faker e salvos com sucesso!");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void exibirRelatorio(Connection conn) throws SQLException {
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
}