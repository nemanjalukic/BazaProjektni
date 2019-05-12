package net.etfbl.bp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.AutoCompleteComboBoxListener;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Dostupnost;

import java.sql.*;


public class DostupnostController extends BaseController {
    @FXML
    private TextField proizvodText;
    @FXML
    private ComboBox<String> sifraComboBox;
    @FXML
    private Button ponistiButton;
    @FXML
    private Button odustaniButton;
    @FXML
    private Button prikaziButton;
    @FXML
    private TableView<Dostupnost> table;

    private ObservableList<String> sifraData;
    private ObservableList<Dostupnost> tableData;

    private CallableStatement cs;
    private PreparedStatement ps;
    private ResultSet rs;

    @FXML
    void initialize() {
        sifraData = FXCollections.observableArrayList();
        tableData = FXCollections.observableArrayList();
        sifraComboBox.setItems(sifraData);
        sifraComboBox.getEditor().setTextFormatter(new TextFormatter<>(change -> (change.getText().matches("\\d*")) ? change : null));
        new AutoCompleteComboBoxListener(sifraComboBox);
        TableColumn[] tc = table.getColumns().toArray(new TableColumn[3]);
        tc[0].setCellValueFactory(new PropertyValueFactory<>("id"));
        tc[1].setCellValueFactory(new PropertyValueFactory<>("adresa"));
        tc[2].setCellValueFactory(new PropertyValueFactory<>("telefon"));
        tc[3].setCellValueFactory(new PropertyValueFactory<>("stanje"));
        table.setItems(tableData);

        try {
            ps = ConnectionPool.getConnection().prepareStatement("SELECT Sifra FROM artikl ORDER BY Sifra ASC");
            rs = ps.executeQuery();
            while (rs.next())
                sifraData.add(rs.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(ps, rs);
        }

        prikaziButton.setOnAction(event -> {
            String sifra = sifraComboBox.getEditor().getText();
            if (sifra.isEmpty())
                Main.showAlertError("Morate unijeti šifru");
            else if (!provjeriSifru(sifra)) {
                Main.showAlertError("Ne postojeći proizvod!");
            } else {
                try {
                    tableData.clear();
                    cs = ConnectionPool.getConnection().prepareCall("CALL getNazivArtikla(?, ?)");
                    cs.setObject(1, Integer.valueOf(sifra));
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.executeUpdate();
                    String nazivArtikla = cs.getString(2);
                    System.out.println(nazivArtikla);
                    cs = ConnectionPool.getConnection().prepareCall("CALL getDostupnostProizvoda(?)");
                    cs.setObject(1, Integer.valueOf(sifra));
                    rs = cs.executeQuery();
                    proizvodText.setText(nazivArtikla);
                    while (rs.next())
                        tableData.add(new Dostupnost(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)));
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs, rs);
                }
            }
        });

        ponistiButton.setOnAction(event -> {
            tableData.clear();
            sifraComboBox.getEditor().clear();
            proizvodText.clear();
        });

        odustaniButton.setOnAction(event -> ponistiButton.fire());
    }

    private boolean provjeriSifru(String sifra) {
        boolean result = false;
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL provjeriSifru(?, ?)");
            cs.setObject(1, sifra);
            cs.registerOutParameter(2, Types.BOOLEAN);
            cs.executeUpdate();
            result = cs.getBoolean(2);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs);
        }
        return result;
    }

    public ObservableList<String> getSifraData() {
        return sifraData;
    }
}

