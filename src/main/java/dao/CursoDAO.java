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
}