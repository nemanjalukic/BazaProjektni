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
import javafx.scene.input.KeyCode;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.AutoCompleteComboBoxListener;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Proizvod;

import java.sql.*;
import static net.etfbl.bp.controllers.PocetnaController.ID_PRODAVNICE;


public class ProdajaController extends BaseController {

    @FXML
    private Button dodajButton;
    @FXML
    private TableView<Proizvod> table;
    @FXML
    private Button obrisiButton;
    @FXML
    private TextField ukupnoText;
    @FXML
    private ComboBox<String> sifraComboBox;
    @FXML
    private ComboBox<String> kupacComboBox;
    @FXML
    private ComboBox<String> artikalComboBox;
    @FXML
    private TextField kolicinaText;
    @FXML
    private Button proslijediButton;
    @FXML
    private Button odustaniButton;

    private DoubleProperty ukupanIznosRacuna;
    private ObservableList<Proizvod> tableData;
    private ObservableList<String> sifraData, artiklData, kupacData;
    private CallableStatement cs;
    private PreparedStatement ps;
    private ResultSet rs;

    @FXML
    void initialize() {
        /* TAB PRODAJA  */
        ukupanIznosRacuna = new SimpleDoubleProperty(0);
        ukupnoText.textProperty().bind(ukupanIznosRacuna.asString("%.2f").concat(" KM"));

        tableData = FXCollections.observableArrayList();
        table.setItems(tableData);

        sifraData = FXCollections.observableArrayList();
        artiklData = FXCollections.observableArrayList();
        kupacData = FXCollections.observableArrayList("Fizicko lice");
        sifraComboBox.setItems(sifraData);
        artikalComboBox.setItems(artiklData);
        kupacComboBox.setItems(kupacData);

        new AutoCompleteComboBoxListener(sifraComboBox);
        new AutoCompleteComboBoxListener(artikalComboBox);

        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getArtikleIzSkladista(?)");
            cs.setObject(1, ID_PRODAVNICE);
            rs = cs.executeQuery();
            while (rs.next()) {
                sifraData.add(rs.getString(1));
                artiklData.add(rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }

        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getKupce()");
            rs = cs.executeQuery();
            while (rs.next()) {
                kupacData.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
        kupacComboBox.getSelectionModel().selectFirst();
        sifraComboBox.getEditor().setTextFormatter(new TextFormatter<>(change -> (change.getText().matches("\\d*")) ? change : null));
        kolicinaText.setTextFormatter(new TextFormatter<>(change -> (change.getText().matches("\\d*")) ? change : null));
        tableData.addListener(new ListChangeListener<Proizvod>() {
            @Override
            public void onChanged(Change<? extends Proizvod> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (Proizvod a : c.getAddedSubList()) {
                            ukupanIznosRacuna.set(ukupanIznosRacuna.get() + a.getVrijednost());
                        }
                    } else if (c.wasRemoved()) {
                        for (Proizvod a : c.getRemoved()) {
                            ukupanIznosRacuna.set(ukupanIznosRacuna.get() - a.getVrijednost());
                        }
                        if (tableData.isEmpty()) {
                            ukupanIznosRacuna.set(0);
                        }
                    }
                }
            }
        });

        pripremiKolone(table);

        sifraComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.matches("\\d{7}")) {
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL getNazivArtikla(?, ?)");
                    cs.setObject(1, newValue);
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.executeUpdate();
                    String nazivArtikla = cs.getString(2);
                    artikalComboBox.getEditor().setText(nazivArtikla);
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs);
                }
            }
        });
        sifraComboBox.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                dodajStavkeNaRacun();
            }
        });
        artikalComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (artiklData.contains(newValue)) {
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL getSifruArtikla(?, ?)");
                    cs.setObject(1, newValue);
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.executeUpdate();
                    Integer sifra = cs.getInt(2);
                    sifraComboBox.getEditor().setText(sifra.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs);
                }
            }
        });
        kolicinaText.setOnAction(event -> dodajStavkeNaRacun());
        dodajButton.setOnAction(event -> dodajStavkeNaRacun());
        dodajButton.setOnKeyPressed(event -> dodajStavkeNaRacun());
        obrisiButton.setOnAction(event -> {
            Proizvod a = table.getSelectionModel().getSelectedItem();
            if (a != null) {
                tableData.remove(a);
            } else {
                Main.showAlertError("Morate izabrati artikl iz tabele!");
            }
        });
        proslijediButton.setOnAction(event -> kreirajRacun());
        odustaniButton.setOnAction(event -> pripremiZaNoviRacun());
        sifraComboBox.requestFocus();

    }

    private void dodajStavkeNaRacun() {
        String sifra = sifraComboBox.getEditor().getText();
        Integer kolicina = 1;
        if (!kolicinaText.getText().isEmpty()) {
            kolicina = Integer.valueOf(kolicinaText.getText());
        }
        if (sifra == null || sifra.isEmpty()) {
            Main.showAlertError("Morate unijeti šifru!");
            sifraComboBox.requestFocus();
        } else if (!provjeriSifru(sifra)) {
            Main.showAlertError("Ne postojeći artikl!");
        } else {
            Integer stanje = getBrojProizvodaUSkladistu(Integer.valueOf(sifra));
            if (stanje >= kolicina) {
                try {
                    cs = ConnectionPool.getConnection().prepareCall("CALL getNazivCijenaArtikla(?)");
                    cs.setObject(1, sifra);
                    rs = cs.executeQuery();
                    while (rs.next()) {
                        Proizvod proizvod = new Proizvod(sifra, rs.getString(1), kolicina, rs.getDouble(2));
                        Integer index = tableData.indexOf(proizvod);
                        if (index == -1) {
                            tableData.add(proizvod);
                        } else {
                            Double p = tableData.get(index).updateKolicina(kolicina);
                            ukupanIznosRacuna.setValue(ukupanIznosRacuna.get() + p);
                            table.refresh();
                        }
                    }
                    pripremiZaNoviUnos();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    ConnectionPool.close(cs, rs);
                }
            } else if (stanje > 0) {
                Main.showAlertWarining("Na stanju je preostale još " + stanje + " proizvoda!");
            } else {
                Main.showAlertWarining("Proizvod nije na stanju!");
            }
        }
    }

    private Integer getBrojProizvodaUSkladistu(Integer sifra) {
        Integer stanje = 0;
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL getBrojProizvodaUSkladistu(?, ?, ?)");
            cs.setObject(1, sifra);
            cs.setObject(2, ID_PRODAVNICE);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.executeUpdate();
            stanje = cs.getInt(3);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs);
        }
        return stanje;
    }

    private void kreirajRacun() {
        if (!tableData.isEmpty()) {
            try {
                ps = ConnectionPool.getConnection().prepareStatement("INSERT INTO fiskalni_racun (Datum, IdPoslovnice) VALUES (current_time, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, ID_PRODAVNICE);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                Integer brojRacuna = rs.getInt(1);
                ps = ConnectionPool.getConnection().prepareStatement("INSERT INTO stavka_racuna VALUES (?, ?, ?, ?)");
                for (Proizvod a : tableData) {
                    ps.setObject(1, brojRacuna);
                    ps.setObject(2, a.getSifra());
                    ps.setObject(3, a.getKolicina());
                    ps.setObject(4, a.getCijena());
                    ps.executeUpdate();
                }

                if (!kupacComboBox.getSelectionModel().getSelectedItem().equals("Fizicko lice")) {
                    String kupac = kupacComboBox.getSelectionModel().getSelectedItem();
                    cs = ConnectionPool.getConnection().prepareCall("CALL getKupac(?)");
                    cs.setObject(1, kupac);
                    rs = cs.executeQuery();
                    rs.next();
                    int IdKupca = rs.getInt(1);

                    ps = ConnectionPool.getConnection().prepareStatement("INSERT INTO faktura_kupovina(ID,IdPoslovnice,DatumKupovine,BrojRacuna) VALUES (?, ?, current_date, ?)");
                    ps.setObject(1, IdKupca);
                    ps.setObject(2, ID_PRODAVNICE);
                    ps.setObject(3, brojRacuna);
                    ps.executeUpdate();

                }
                pripremiZaNoviRacun();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionPool.close(cs, ps, rs);
            }

        }
    }

    private void pripremiZaNoviRacun() {
        if (!tableData.isEmpty()) {
            tableData.clear();
            ukupanIznosRacuna.set(0.0);
            pripremiZaNoviUnos();
        }
    }

    private void pripremiZaNoviUnos() {
        kolicinaText.clear();
        artikalComboBox.getEditor().clear();
        sifraComboBox.getEditor().clear();
        sifraComboBox.requestFocus();
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

    private void pripremiKolone(TableView<Proizvod> tableView) {
        TableColumn[] c = tableView.getColumns().toArray(new TableColumn[6]);
        ((TableColumn<Proizvod, Object>) c[0]).setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(tableView.getItems().indexOf(column.getValue()) + 1));
        c[1].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("sifra"));
        c[2].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("naziv"));
        c[3].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("kolicina"));
        c[4].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("cijena"));
        c[5].setCellValueFactory(new PropertyValueFactory<Proizvod, String>("vrijednost"));
    }

    public ObservableList<String> getSifraData() {
        return sifraData;
    }
}
