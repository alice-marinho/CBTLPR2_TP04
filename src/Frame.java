import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Frame extends JFrame {

    private JTextField lbNomePesquisa, lbNome, lbSalario, lbCargo;
    private JButton btnPesquisar, btnProximo, btnAnterior;
    private List<Funcionario> funcionarios;
    private int currentIndex = -1;

    public Frame() {

        setTitle("TRABALHO PRATICO 04");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //pesquisa
        JPanel painelPesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        painelPesquisa.add(new JLabel("Nome: "));
        lbNomePesquisa = new JTextField(25);
        painelPesquisa.add(lbNomePesquisa);

        // botão de pesquisa
        JPanel painelBTNPesquisa = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        btnPesquisar = new JButton("Pesquisar");
        painelBTNPesquisa.add(btnPesquisar);

        // exibição de dados
        JPanel painelDados = new JPanel(new GridLayout(4, 2, 5, 5));
        painelDados.add(new JLabel("Nome: "));
        lbNome = new JTextField();
        lbNome.setEditable(false);
        painelDados.add(lbNome);

        painelDados.add(new JLabel("Salario: "));
        lbSalario = new JTextField();
        lbSalario.setEditable(false);
        painelDados.add(lbSalario);

        painelDados.add(new JLabel("Cargo: "));
        lbCargo = new JTextField();
        lbCargo.setEditable(false);
        painelDados.add(lbCargo);

        btnAnterior = new JButton("Anterior");
        painelDados.add(btnAnterior);

        btnProximo = new JButton("Proximo");
        painelDados.add(btnProximo);

        add(painelPesquisa, BorderLayout.NORTH);
        add(painelBTNPesquisa, BorderLayout.CENTER);
        add(painelDados, BorderLayout.SOUTH);

        // Inicializa os funcionários e exibe o primeiro funcionário
        carregarFuncionarios();
        mostrarFuncionarioAtual();

        // Configuração dos botões
        btnPesquisar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pesquisarFuncionarios();
            }
        });

        btnAnterior.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarFuncionarioAnterior();
            }
        });

        btnProximo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarProximoFuncionario();
            }
        });
    }

    // Carrega todos os funcionários do banco de dados e preenche a lista
    private void carregarFuncionarios() {
        funcionarios = new ArrayList<>();
        try {
            Connection connection = Conexao.getConnection();
            if (connection != null) {
                String sql = "SELECT tbfuncs.cod_func, tbfuncs.nome_func, tbfuncs.sal_func, tbcargos.ds_cargo " +
                        "FROM tbfuncs JOIN tbcargos ON tbfuncs.cod_cargo = tbcargos.cd_cargo;";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int codFunc = resultSet.getInt("cod_func");
                    String nomeFunc = resultSet.getString("nome_func");
                    double salFunc = resultSet.getDouble("sal_func");
                    String dsCargo = resultSet.getString("ds_cargo");
                    funcionarios.add(new Funcionario(codFunc, nomeFunc, salFunc, dsCargo));
                }
                resultSet.close();
                preparedStatement.close();
                connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar os funcionários.");
        }
    }

    // Exibe os dados do funcionário atual
    private void mostrarFuncionarioAtual() {
        if (!funcionarios.isEmpty() && currentIndex >= 0 && currentIndex < funcionarios.size()) {
            Funcionario funcionario = funcionarios.get(currentIndex);
            lbNome.setText(funcionario.getNome());
            lbSalario.setText(String.valueOf(funcionario.getSalario()));
            lbCargo.setText(funcionario.getCargo());

            // Atualiza a habilitação dos botões
            btnAnterior.setEnabled(currentIndex > 0); // Desabilita "Anterior" no primeiro funcionário
            btnProximo.setEnabled(currentIndex < funcionarios.size() - 1); // Desabilita "Próximo" no último funcionário
        }
    }

    // Exibe o próximo funcionário
    private void mostrarProximoFuncionario() {
        if (currentIndex < funcionarios.size() - 1) {
            currentIndex++;
            mostrarFuncionarioAtual();
        } else {
            JOptionPane.showMessageDialog(this, "Você já está no último funcionário.");
        }
    }

    // Exibe o funcionário anterior
    private void mostrarFuncionarioAnterior() {
        if (currentIndex > 0) {
            currentIndex--;
            mostrarFuncionarioAtual();
        } else {
            JOptionPane.showMessageDialog(this, "Você já está no primeiro funcionário.");
        }
    }

    // Pesquisa os funcionários com base no nome
    private void pesquisarFuncionarios() {
        String nomePesquisa = lbNomePesquisa.getText();
        funcionarios.clear(); // Limpa a lista atual de funcionários

        try {
            Connection connection = Conexao.getConnection();
            if (connection != null) {
                String sql;

                // Se o campo de pesquisa estiver vazio, carrega todos os funcionários
                if (nomePesquisa.trim().isEmpty()) {
                    sql = "SELECT tbfuncs.cod_func, tbfuncs.nome_func, tbfuncs.sal_func, tbcargos.ds_cargo " +
                            "FROM tbfuncs JOIN tbcargos ON tbfuncs.cod_cargo = tbcargos.cd_cargo;";
                } else {
                    sql = "SELECT tbfuncs.cod_func, tbfuncs.nome_func, tbfuncs.sal_func, tbcargos.ds_cargo " +
                            "FROM tbfuncs JOIN tbcargos ON tbfuncs.cod_cargo = tbcargos.cd_cargo " +
                            "WHERE tbfuncs.nome_func LIKE ?;";
                }

                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                // Se houver pesquisa, adiciona o LIKE
                if (!nomePesquisa.trim().isEmpty()) {
                    preparedStatement.setString(1, "%" + nomePesquisa + "%");
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int codFunc = resultSet.getInt("cod_func");
                    String nomeFunc = resultSet.getString("nome_func");
                    double salFunc = resultSet.getDouble("sal_func");
                    String dsCargo = resultSet.getString("ds_cargo");
                    funcionarios.add(new Funcionario(codFunc, nomeFunc, salFunc, dsCargo));
                }
                resultSet.close();
                preparedStatement.close();
                connection.close();

                // Se encontrou funcionários, exibe o primeiro
                if (!funcionarios.isEmpty()) {
                    currentIndex = 0; // Inicia no primeiro funcionário da nova lista
                    mostrarFuncionarioAtual();
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum funcionário encontrado.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao pesquisar funcionários.");
        }
    }

}
