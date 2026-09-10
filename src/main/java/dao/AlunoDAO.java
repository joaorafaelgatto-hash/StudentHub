package dao;
import com.github.javafaker.Faker;
import java.util.Locale;
import java.util.Random;
import model.Aluno;
import util.ConexaoBanco;
import java.sql.*;
import java.util.List;

public class AlunoDAO {
    /*public void salvarEmLote(List<Aluno>alunos) throws SQLException {
        String sql = "INSERT INTO alunos (nome, curso_id) VALUES (?, ?);";
        try(Connection conn = ConexaoBanco.getConnection()) {
            conn.setAutoCommit(false);
            try(PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < alunos.size(); i++) {
                    Aluno aluno = alunos.get(i);
                    stmt.setString(1, aluno.getNome());
                    stmt.setInt(2, aluno.getCurso().getId());
                    stmt.addBatch();
                    if((i+1) % 1000 == 0) {
                        stmt.executeBatch();
                    }
                }
                stmt.executeBatch();
                conn.commit();
            }catch(SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }*/

    //jr: os dois métodos fazerm meio que fazem a mesma coisa
    //temos que ver oq é mais interessante, esse primeiro não está em uso no momento

    public static void inserirAlunosComFaker(Connection conn, List<Integer> cursosIds, int quantidade) throws SQLException {
        //da play no framework
        Faker faker = new Faker(new Locale("pt-BR"));
        Random random = new Random();

        String sql = "INSERT INTO alunos (nome, curso_id) VALUES (?, ?);";
        conn.setAutoCommit(false); // Transacao em lote

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= quantidade; i++) {
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
            System.out.println(quantidade + " alunos gerados com Faker e salvos com sucesso!");
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
