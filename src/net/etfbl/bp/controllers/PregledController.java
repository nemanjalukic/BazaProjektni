package net.etfbl.bp.controllers;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import net.etfbl.bp.Main;
import net.etfbl.bp.helper.AutoCompleteComboBoxListener;
import net.etfbl.bp.helper.ConnectionPool;
import net.etfbl.bp.helper.Proizvod;
import java.sql.*;
import static net.etfbl.bp.controllers.PocetnaController.ID_PRODAVNICE;


public class PregledController extends BaseController {

    @FXML
    private TextField ukupanIznosNabavkeText;

    @FXML
    private TextField poslovnicaText;

    @FXML
    private TextField datumText;

    @FXML
    private ComboBox<String> vrstaComboBox;

    @FXML
    private Label pravnoLabel;

    @FXML
    private Label datumLabel;

    @FXML
    private Button ponistiButton;

    @FXML
    private Button uplatitiButton;

    @FXML
    private TableView<Proizvod> pregledNabavkiTable;

    @FXML
    private TextField preostaloZaUplatuText;

    @FXML
    private TextField iznosText;

    @FXML
    private ComboBox<String> brojFaktureComboBox;

    @FXML
    private TextField uplacenoText;

    @FXML
    private TextField dobavljacText;

    @FXML
    private Button prikaziButton;
    private ObservableList<Proizvod> stavkeNabavke;
    private ObservableList<String> brojeviFaktura;
    private ObservableList<String> vrs;
    private CallableStatement cs;
    private ResultSet rs;
    private PreparedStatement ps;

    private DoubleProperty ukupanIznosNabavke;

    @FXML
    void initialize() {
        pripremiKolone(pregledNabavkiTable);
        stavkeNabavke = FXCollections.observableArrayList();
        vrs = FXCollections.observableArrayList();
        vrs.add("Nabavka");
        vrs.add("Kupovina");
        vrstaComboBox.setItems(vrs);
        vrstaComboBox.getSelectionModel().selectFirst();
        pregledNabavkiTable.setItems(stavkeNabavke);
        brojeviFaktura = FXCollections.observableArrayList();
        new AutoCompleteComboBoxListener(brojFaktureComboBox);
        brojFaktureComboBox.setItems(brojeviFaktura);
        brojFaktureComboBox.getEditor().setTextFormatter(new TextFormatter<String>(change -> (change.getText().matches("\\d*")) ? change : null));
        try {
            cs = ConnectionPool.getConnection().prepareCall("SELECT BrojFakture FROM faktura_nabavka");
            rs = cs.executeQuery();
            while (rs.next()) {
                brojeviFaktura.add(rs.getInt(1) + "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionPool.close(cs, rs);
        }
        prikaziButton.setOnAction(event -> prikaziNabavku());
        uplatitiButton.setOnAction(event -> uplatiti());
        vrstaComboBox.setOnAction(event -> izmijene());
        ukupanIznosNabavke = new SimpleDoubleProperty(0);
        ukupanIznosNabavkeText.textProperty().bind(ukupanIznosNabavke.asString("%.2f").concat(" KM"));
    }

    private void prikaziNabavku() {
        if (brojFaktureComboBox.getEditor().getText().isEmpty()) {
            Main.showAlertError("Morate unijeti broj fakture!");
        } else {
            if (vrstaComboBox.getSelectionModel().getSelectedItem().equals("Nabavka")) {
                Integer brojFakture = Integer.valueOf(brojFaktureComboBox.getEditor().getText());
                if (provjeriBrojFakture(brojFakture)) {
                    try {
                        pripremiZaNoviPrikaz();
                        cs = ConnectionPool.getConnection().prepareCall("CALL getInfoZaNabavku(?, ?, ?, ?, ?, ?, ?)");
                        cs.setObject(1, brojFakture);
                        cs.registerOutParameter(2, Types.INTEGER);
                        cs.registerOutParameter(3, Types.VARCHAR);
                        cs.registerOutParameter(4, Types.VARCHAR);
                        cs.registerOutParameter(5, Types.DATE);
                        cs.registerOutParameter(6, Types.DOUBLE);
                        cs.registerOutParameter(7, Types.DOUBLE);
                        rs = cs.executeQuery();
                        Integer poslovnica = cs.getInt(2);
                        String adresa = cs.getString(3);
                        String nazivDobavljaca = cs.getString(4);
                        Date datumNabavke = cs.getDate(5);
                        Double ukupanIznos = cs.getDouble(6);
                        Double uplaceno = cs.getDouble(7);
                        poslovnicaText.setText("NemanjaBoje " + poslovnica + " - " + adresa);
                        datumText.setText(datumNabavke.toString());
                        dobavljacText.setText(nazivDobavljaca);
                        ukupanIznosNabavke.set(ukupanIznos);
                        uplacenoText.setText(uplaceno + " KM");
                        preostaloZaUplatuText.setText((ukupanIznos - uplaceno) + " KM");
                        while (rs.next()) {
                            stavkeNabavke.add(new Proizvod(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        ConnectionPool.close(cs, rs);
                    }
                } else {
                    Main.showAlertWarining("Broj fakture nije validan!");
                }
            } else {
                Integer brojFakture = Integer.valueOf(brojFaktureComboBox.getEditor().getText());
                if (provjeriBrojFaktureKupovina(brojFakture)) {
                    try {
                        pripremiZaNoviPrikaz();
                        cs = ConnectionPool.getConnection().prepareCall("CALL getInfoZaKupovinu(?, ?, ?, ?, ?, ?, ?)");
                        cs.setObject(1, brojFakture);
                        cs.registerOutParameter(2, Types.INTEGER);
                        cs.registerOutParameter(3, Types.VARCHAR);
                        cs.registerOutParameter(4, Types.VARCHAR);
                        cs.registerOutParameter(5, Types.DATE);
                        cs.registerOutParameter(6, Types.DOUBLE);
                        cs.registerOutParameter(7, Types.DOUBLE);
                        rs = cs.executeQuery();
                        Integer poslovnica = cs.getInt(2);
                        String adresa = cs.getString(3);
                        String nazivDobavljaca = cs.getString(4);
                        Date datumNabavke = cs.getDate(5);
                        Double ukupanIznos = cs.getDouble(6);
                        Double uplaceno = cs.getDouble(7);
                        poslovnicaText.setText("NemanjaBoje " + poslovnica + " - " + adresa);
                        datumText.setText(datumNabavke.toString());
                        dobavljacText.setText(nazivDobavljaca);
                        ukupanIznosNabavke.set(ukupanIznos);
                        uplacenoText.setText(uplaceno + " KM");
                        preostaloZaUplatuText.setText((ukupanIznos - uplaceno) + " KM");
                        while (rs.next()) {
                            stavkeNabavke.add(new Proizvod(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getDouble(4)));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        ConnectionPool.close(cs, rs);
                    }
                } else {
                    Main.showAlertWarining("Broj fakture nije validan!");
                }

            }
        }
        ponistiButton.setOnAction(event -> {
            brojFaktureComboBox.getEditor().clear();
            pripremiZaNoviPrikaz();
        });
    }

    private boolean provjeriBrojFakture(Integer brojFakture) {
        boolean result = false;
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL provjeriValidnostBrojaFakture(?, ?)");
            cs.setObject(1, brojFakture);
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

    private boolean provjeriBrojFaktureKupovina(Integer brojFakture) {
        boolean result = false;
        try {
            cs = ConnectionPool.getConnection().prepareCall("CALL provjeriValidnostBrojaFaktureKupovina(?, ?)");
            cs.setObject(1, brojFakture);
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

    private void pripremiZaNoviPrikaz() {
        poslovnicaText.clear();
        datumText.clear();
        dobavljacText.clear();
        stavkeNabavke.clear();
        uplacenoText.clear();
        preostaloZaUplatuText.clear();
        ukupanIznosNabavkeText.clear();
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

    public ObservableList<String> getBrojeviFaktura() {
        return brojeviFaktura;
    }

    private void izmijene() {
        if (vrstaComboBox.getSelectionModel().getSelectedItem().equals("Kupovina")) {
            try {
                cs = ConnectionPool.getConnection().prepareCall("SELECT BrojFakture FROM faktura_kupovina");
                rs = cs.executeQuery();
                brojeviFaktura.clear();
                while (rs.next()) {
                    brojeviFaktura.add(rs.getInt(1) + "");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionPool.close(cs, rs);
            }
            datumLabel.setText("Datum kupovine");
            pravnoLabel.setText("Kupac");
            pripremiZaNoviPrikaz();

        } else {
            try {
                cs = ConnectionPool.getConnection().prepareCall("SELECT BrojFakture FROM faktura_nabavka");
                rs = cs.executeQuery();
                brojeviFaktura.clear();
                while (rs.next()) {
                    brojeviFaktura.add(rs.getInt(1) + "");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionPool.close(cs, rs);
            }
            datumLabel.setText("Datum nabavke");
            pravnoLabel.setText("Dobavljač");
            pripremiZaNoviPrikaz();

        }
    }

    private void uplatiti() {
        if (vrstaComboBox.getSelectionModel().getSelectedItem().equals("Kupovina")) {
            try {

                cs = ConnectionPool.getConnection().prepareCall("CALL getKupac(?)");
                cs.setObject(1, dobavljacText.getText());
                rs = cs.executeQuery();
                rs.next();
                int id = rs.getInt(1);

                cs = ConnectionPool.getConnection().prepareCall("CALL getRacunPoslovnice(?)");
                cs.setObject(1, ID_PRODAVNICE);
                rs = cs.executeQuery();
                rs.next();
                String brRac = rs.getString(1);

                ps = ConnectionPool.getConnection().prepareStatement("INSERT INTO uplata_kupca VALUES (null,?, ?, ?,current_date, ?)");
                ps.setObject(1, brRac);
                ps.setObject(2, id);
                ps.setObject(3, Double.parseDouble(iznosText.getText()));
                ps.setObject(4, Integer.valueOf(brojFaktureComboBox.getEditor().getText()));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionPool.close(cs, ps, rs);
            }
        } else {
            try {

                cs = ConnectionPool.getConnection().prepareCall("CALL getKupac(?)");
                cs.setObject(1, dobavljacText.getText());
                rs = cs.executeQuery();
                rs.next();
                int id = rs.getInt(1);

                cs = ConnectionPool.getConnection().prepareCall("CALL getRacunPoslovnice(?)");
                cs.setObject(1, ID_PRODAVNICE);
                rs = cs.executeQuery();
                rs.next();
                String brRac = rs.getString(1);

                ps = ConnectionPool.getConnection().prepareStatement("INSERT INTO isplata_dobavljacu VALUES (null,current_date,?, ?, ?, ?)");
                ps.setObject(1, Double.parseDouble(iznosText.getText()));
                ps.setObject(2, id);
                ps.setObject(3, brRac);
                ps.setObject(4, Integer.valueOf(brojFaktureComboBox.getEditor().getText()));
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                ConnectionPool.close(cs, ps, rs);
            }

        }
        prikaziNabavku();

    }

}
