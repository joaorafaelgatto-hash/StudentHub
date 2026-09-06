package dao;

import model.Aluno;
import util.ConexaoBanco;
import java.sql.*;
import java.util.List;

public class AlunoDAO {
    public void salvarEmLote(List<Aluno>alunos) throws SQLException {
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
    }
}
