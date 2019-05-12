package net.etfbl.bp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Promet;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrometController {
    @FXML
    private ComboBox<String> prometComboBox;

    @FXML
    private ComboBox<String> poslovnicaComboBox;

    @FXML
    private TableColumn<Promet, String> dateColumn;

    @FXML
    private Button prikaziButton;

    @FXML
    private TableView<Promet> table;

    private PreparedStatement ps;
    private CallableStatement cs;
    private ResultSet rs;

    private ObservableList<String> poslovnice, promet;
    private ObservableList<Promet> tableData;

    @FXML
    void initialize() {
        poslovnice = FXCollections.observableArrayList("*");
        poslovnicaComboBox.setItems(poslovnice);
        promet = FXCollections.observableArrayList();
        prometComboBox.setItems(promet);
        tableData = FXCollections.observableArrayList();
        table.setItems(tableData);
        pripremiKolone(table);

        poslovnice.addAll(getPoslovnice());
        promet.addAll("Dnevni", "Mjesecni", "Godisnji");

        prikaziButton.setOnAction(event -> {
            if (poslovnicaComboBox.getValue() == null)
                Main.showAlertError("Morate izabrati neku poslovnicu ili staviti *.");
            else if (prometComboBox.getValue() == null)
                Main.showAlertError("Morate izabrati period za prikaz prometa.");
            else {
                pripremiZaNoviPrikaz();
                prikaziPromet();
            }
        });
    }

    private void pripremiZaNoviPrikaz(){
        tableData.clear();
        switch (prometComboBox.getValue()) {
            case "Dnevni":
                dateColumn.setText("Datum");
                break;
            case "Mjesecni":
                dateColumn.setText("Mjesec");
                break;
            case "Godisnji":
                dateColumn.setText("Godina");
        }
    }

    private void prikaziPromet() {
        String poslovnica = poslovnicaComboBox.getValue();
        String promet = prometComboBox.getValue();
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getPromet(?, ?)");
            cs.setString(1, poslovnica);
            cs.setString(2, promet);
            rs = cs.executeQuery();
            while (rs.next())
                tableData.add(new Promet(rs.getString(1), rs.getDouble(2)));
            table.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
    }

    private ObservableList<String> getPoslovnice() {
        ObservableList<String> list = FXCollections.observableArrayList();
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getPoslovnice()");
            rs = cs.executeQuery();
            while (rs.next())
                list.add(rs.getString(2));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
        return list;
    }

    private void pripremiKolone(TableView<Promet> tableView) {
        TableColumn[] c = tableView.getColumns().toArray(new TableColumn[2]);
        c[0].setCellValueFactory(new PropertyValueFactory<Promet, String>("datum"));
        c[1].setCellValueFactory(new PropertyValueFactory<Promet, String>("iznos"));
    }
}
