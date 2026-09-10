
// FLUXO GIT PARA NÃO NOS PERDERMOS
//ANTES de começar a programar qualquer coisa, SEMPRE abra o Git Bash e rode:
//    git pull origin main
//    (Isso atualiza o nsso projeto local com tudo o que ja foi mexido)
//
// 2- Faça suas alterações necessárias e teste a execução no IntelliJ.
//
// 3 -para enviar suas atualizações para o GitHub, execute no git bash
//    git add .
//    git commit -m "tipo: descreva aqui"
//    git pull --rebase origin main
//    git push origin main
//
//O 'git pull --rebase origin main' antes do push garante que, caso alguém tenha
//   enviado código enquanto você digitava, as mudanças se juntem sem apagar o trabalho de ngm



import com.github.javafaker.Faker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Main {

    // Config de conexão com o banco
    private static final String URL = "jdbc:postgresql://localhost:5432/URI";
    private static final String USER = "postgres";
    private static final String PASSWORD = "avatar"; //senha

    public static void main(String[] args) {
        System.out.println("Iniciando processamento do StudentHub...");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println(">> Conectado com sucesso ao PostgreSQL!");

            limparBanco(conn);

            //lista de cursos aprimorada (estruturada pelo joao rafael)
            List<String> nomesCursos = Arrays.asList(
                    "Ciencia da Computacao", "Engenharia de Software", "Sistemas de Informacao",
                    "Direito", "Administracao", "Medicina", "Agronomia",
                    "Psicologia", "Enfermagem", "Arquitetura"
            );


                //cadastro dos cursos e captura dinâmica dos IDs
            List<Integer> cursosIds = cadastrarCursos(conn, nomesCursos);

            // lote de 5.000 alunos via JavaFaker
            inserirAlunosComFaker(conn, cursosIds, 5000);


            exibirRelatorio(conn);


            //listagem dos 50 primeiros alunos no console e exportação das 5.000 linhas em TXT
            listarAlunosDetalhados(conn);

            System.out.println("\n>> Processamento concluído com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro na execução do banco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Limpa as tabelas existentes e reinicia o contador SERIAL (id = 1).
     * ggarante que toda vez que o programa rodar, o banco comece limpo.
     */
    private static void limparBanco(Connection conn) throws SQLException {
        String sql = "TRUNCATE TABLE alunos, cursos RESTART IDENTITY CASCADE;";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println(">> Banco limpo com sucesso! Sequências de IDs reiniciadas.");
        }
    }

    /**
     * cad a lista de cursos e utiliza 'RETURNING id' para capturar
     *
     * na hora os IDs gerados pelo banco sem precisar de consultas extras.
     */
    private static List<Integer> cadastrarCursos(Connection conn, List<String> nomesCursos) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "INSERT INTO cursos (nome) VALUES (?) RETURNING id;";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String nomeCurso : nomesCursos) {
                stmt.setString(1, nomeCurso);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ids.add(rs.getInt("id"));
                    }
                }
            }
        }
        System.out.println(">> " + ids.size() + " cursos cadastrados com sucesso!");
        return ids;
    }

    /**
     * Gera 5.000 nomes fictícios  com javaFaker e insere em (batch)
     *
     */
    private static void inserirAlunosComFaker(Connection conn, List<Integer> cursosIds, int totalAlunos) throws SQLException {
        String sql = "INSERT INTO alunos (nome, curso_id) VALUES (?, ?);";
        Faker faker = new Faker(new Locale("pt-BR"));
        Random random = new Random();

        // desliga o commit automático para gerenciar a transação manualmente
        conn.setAutoCommit(false);

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            System.out.println(">> Gerando e inserindo " + totalAlunos + " alunos em lote...");

            for (int i = 1; i <= totalAlunos; i++) {
                String nomeCompleto = faker.name().fullName();
                int cursoIdSorteado = cursosIds.get(random.nextInt(cursosIds.size()));

                stmt.setString(1, nomeCompleto);
                stmt.setInt(2, cursoIdSorteado);
                stmt.addBatch(); //add o comando na fila de memória

                // Envia para o banco em blocos de 1.000 para não estourar a memória
                if (i % 1000 == 0 || i == totalAlunos) {
                    stmt.executeBatch();
                    System.out.println("   Lote processado: " + i + " registros inseridos...");
                }
            }

            conn.commit(); // verifica a gravação de todos os alunos
            System.out.println(">> Todos os " + totalAlunos + " alunos foram persistidos com sucesso!");

        } catch (SQLException e) {
            conn.rollback(); //se der errado
            throw e;
        } finally {
            conn.setAutoCommit(true); //Restaura o comportamento padrão da conexão
        }
    }

    /**
     * Consulta analítica no banco agrupando a quantidade de alunos por curso
     * e exibindo em ordem no console.
     */
    private static void exibirRelatorio(Connection conn) throws SQLException {
        String sql = """
            SELECT c.nome AS curso, COUNT(a.id) AS total_alunos
            FROM cursos c
            LEFT JOIN alunos a ON c.id = a.curso_id
            GROUP BY c.nome
            ORDER BY total_alunos DESC;
        """;

        System.out.println("\n================ RELATÓRIO QUANTITATIVO POR CURSO ================");
        System.out.printf("%-30s | %-15s\n", "CURSO", "TOTAL DE ALUNOS");
        System.out.println("------------------------------------------------------------------");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String curso = rs.getString("curso");
                int total = rs.getInt("total_alunos");
                System.out.printf("%-30s | %-15d\n", curso, total);
            }
        }
        System.out.println("==================================================================\n");
    }

    /**
     * Exibe os primeiros 50 alunos com seus respectivos cursos no console
     * e exporta a relação completa de todos os 5.000 alunos para 'relatorio_5000_alunos.txt' verificar o doc antes
     */
    private static void listarAlunosDetalhados(Connection conn) {
        String sql = """
            SELECT a.id, a.nome AS aluno_nome, c.nome AS curso_nome
            FROM alunos a
            JOIN cursos c ON a.curso_id = c.id
            ORDER BY a.id ASC;
        """;

        System.out.println("\n--- AMOSTRA DOS 50 PRIMEIROS ALUNOS CADASTRADOS ---");
        System.out.printf("%-6s | %-35s | %-25s\n", "ID", "NOME DO ALUNO", "CURSO");
        System.out.println("-----------------------------------------------------------------------");

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter("relatorio_5000_alunos.txt"))) {

            writer.write("=======================================================================\n");
            writer.write(String.format("%-6s | %-35s | %-25s\n", "ID", "NOME DO ALUNO", "CURSO"));
            writer.write("=======================================================================\n");

            int contador = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String nomeAluno = rs.getString("aluno_nome");
                String nomeCurso = rs.getString("curso_nome");

                // Grava todas as 5.000 linhas no arquivo TXT físico
                writer.write(String.format("%-6d | %-35s | %-25s\n", id, nomeAluno, nomeCurso));

                // Exibe no console apenas os primeiros 50 registros para verificação
                if (contador < 50) {
                    System.out.printf("%-6d | %-35s | %-25s\n", id, nomeAluno, nomeCurso);
                    contador++;
                }
            }

            System.out.println("-----------------------------------------------------------------------");
            System.out.println(">> Arquivo 'relatorio_5000_alunos.txt' gerado com sucesso com 5.000 registros!");

        } catch (SQLException | IOException e) {
            System.err.println("Erro ao listar ou exportar alunos: " + e.getMessage());
        }
    }
}