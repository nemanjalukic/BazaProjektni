package net.etfbl.bp.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Proizvod;

import java.sql.*;
import static net.etfbl.bp.controllers.PocetnaController.ID_PRODAVNICE;


public class StoniranjeController extends BaseController {

    @FXML
    private TextField brojRacunaText;

    @FXML
    private Button stonirajButton;

    @FXML
    private TextField ukupnoText;

    @FXML
    private Button odustaniButton;

    @FXML
    private Button prikaziButton;

    @FXML
    private TableView<Proizvod> table;

    private DoubleProperty ukupanIznos;
    private ObservableList<Proizvod> tableData;
    private CallableStatement cs;
    private PreparedStatement ps;
    private ResultSet rs;

    @FXML
    void initialize() {
        ukupanIznos = new SimpleDoubleProperty(0);
        ukupnoText.textProperty().bind(ukupanIznos.asString("%.2f").concat(" KM"));
        brojRacunaText.setTextFormatter(new TextFormatter<>(change -> (change.getText().matches("\\d*")) ? change : null));
        tableData = FXCollections.observableArrayList();
        pripremiKolone(table);
        table.setItems(tableData);

        tableData.addListener(new ListChangeListener<Proizvod>() {
            @Override
            public void onChanged(Change<? extends Proizvod> c) {
                while (c.next())
                    if (c.wasAdded())
                        for (Proizvod a : c.getAddedSubList())
                            ukupanIznos.set(ukupanIznos.get() + a.getVrijednost());
            }
        });

        prikaziButton.setOnAction(event -> {
            ocistiZaStariRacun();
            if (brojRacunaText.getText().isEmpty()) {
                Main.showAlertError("Morate unijeti broj računa!");
                brojRacunaText.requestFocus();
            } else if (isBrojRacunaValidan(Integer.valueOf(brojRacunaText.getText()))) {
                prikaziStavkeRacunaZaStoniranje(Integer.valueOf(brojRacunaText.getText()));
            } else
                Main.showAlertError("Broj računa nije validan!");
        });

        stonirajButton.setOnAction(event -> {
            if (brojRacunaText.getText().isEmpty()) {
                Main.showAlertError("Morate unijeti broj računa!");
                brojRacunaText.requestFocus();
            } else if (isBrojRacunaValidan(Integer.valueOf(brojRacunaText.getText()))) {
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL stonirajRacun(?, ?)");
                    cs.setObject(1, Integer.valueOf(brojRacunaText.getText()));
                    cs.setInt(2, ID_PRODAVNICE);
                    cs.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs, rs, ps);
                }
                ocistiZaStariRacun();
                brojRacunaText.clear();
            }
        });

        odustaniButton.setOnAction(event -> {
            brojRacunaText.clear();
            ocistiZaStariRacun();
        });

    }

    private void ocistiZaStariRacun() {
        ukupanIznos.set(0);
        tableData.clear();
    }

    private boolean isBrojRacunaValidan(Integer brojRacuna) {
        boolean isValidan = false;
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL provjeriValidnostRacuna(?, ?)");
            cs.setObject(1, brojRacuna);
            cs.registerOutParameter(2, Types.BOOLEAN);
            cs.executeUpdate();
            isValidan = cs.getBoolean(2);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs);
        }
        return isValidan;
    }

    private void prikaziStavkeRacunaZaStoniranje(Integer brojRacuna) {
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getStavkeRacuna(?)");
            cs.setObject(1, brojRacuna);
            rs = cs.executeQuery();
            while (rs.next())
                tableData.add(new Proizvod(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
    }

    private void pripremiKolone(TableView<Proizvod> tableView) {
        TableColumn[] c = tableView.getColumns().toArray(new TableColumn[6]);
        ((TableColumn<Proizvod, Number>) c[0]).setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(column.getValue()) + 1));
        c[1].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("sifra"));
        c[2].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("naziv"));
        c[3].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("kolicina"));
        c[4].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("cijena"));
        c[5].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("vrijednost"));
    }
}