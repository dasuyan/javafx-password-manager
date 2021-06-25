package App.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static App.login.Controller.file;
import static App.login.Controller.lastSuccessfulTS;


public class Controller {
    @FXML
    public TableView<Password> tableView;
    @FXML
    public TableColumn<Password, String> columnName;
    @FXML
    public TableColumn<Password, String> columnPassword;
    @FXML
    public TableColumn<Password, Category> columnCategory;
    @FXML
    public Label timestamp;
    @FXML
    public Button addPButton;
    @FXML
    public Button removePButton;
    @FXML
    public Button addCButton;
    @FXML
    public Button removeCButton;
    @FXML
    public ComboBox<Category> categoryBox = new ComboBox<>();

    public ComboBox<Category> comboBoxAP = new ComboBox<>();
    public ComboBox<Category> comboBoxRC = new ComboBox<>();

    public static ObservableList<Password> passwordList = FXCollections.observableArrayList();
    public static ObservableList<Password> filteredList = FXCollections.observableArrayList();
    public static ObservableList<Category> categoryList = FXCollections.observableArrayList();

    private final static String passwordListKey = "this is a secret";
    private final static String categoryListKey = "Another secret";


    public void initialize() {
    timestamp.setText(lastSuccessfulTS);
        loadTableData();
        loadComboBoxData();
    }

    public void loadFromFile() {
        BufferedReader objReader = null;
        try {
            String line = null;
            objReader = new BufferedReader(new FileReader(App.login.Controller.file));

            for (int i = 0; i < 3; i++)
                line = objReader.readLine();

            String passwordListString = removeFirstAndLast(AES.decrypt(line, passwordListKey));
            String[] outer1 = passwordListString.split(", ");

            line = objReader.readLine();
            String categoryListString = removeFirstAndLast(AES.decrypt(line, categoryListKey));
            String[] cLS2 = categoryListString.split(", ");

            for (String value : cLS2) {
                Category category = new Category(value);
                categoryList.add(category);
            }

            Category remakeC = null;

            for (String s : outer1) {
                String[] inner = s.split(":");
                for (Category category : categoryList) {
                    if (inner[2].equals(category.getCategory()))
                        remakeC = category;
                }
                if (inner.length == 3) {
                    Password record = new Password(inner[0], inner[1], remakeC);
                    passwordList.add(record);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objReader != null)
                    objReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadTableData() {
        loadFromFile();

        filteredList.addAll(passwordList);

        passwordList.addListener((ListChangeListener<Password>) change -> updateFilteredData());

        tableView.setPlaceholder(new Label("No passwords to display"));

        tableView.setEditable(true);
        Callback<TableColumn<Password, String>, TableCell<Password, String>> cellFactory
                = (TableColumn<Password, String> param) -> new EditingCell();
        Callback<TableColumn<Password, Category>, TableCell<Password, Category>> comboBoxCellFactory
                = (TableColumn<Password, Category> param) -> new ComboBoxEditingCell();

        columnName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        columnName.setCellFactory(cellFactory);
        columnName.setOnEditCommit(
                (TableColumn.CellEditEvent<Password, String> t) -> t.getTableView().getItems()
                        .get(t.getTablePosition().getRow())
                        .setName(t.getNewValue()));

        columnPassword.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
        columnPassword.setCellFactory(cellFactory);
        columnPassword.setOnEditCommit(
                (TableColumn.CellEditEvent<Password, String> t) -> t.getTableView().getItems()
                        .get(t.getTablePosition().getRow())
                        .setName(t.getNewValue()));

        columnCategory.setCellValueFactory(cellData -> cellData.getValue().categoryObjProperty());
        columnCategory.setCellFactory(comboBoxCellFactory);
        columnCategory.setOnEditCommit(
                (TableColumn.CellEditEvent<Password, Category> t) -> t.getTableView().getItems()
                        .get(t.getTablePosition().getRow())
                        .setCategoryObj(t.getNewValue()));

        tableView.setItems(filteredList);

        categoryBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilteredData());

    }

    private void updateFilteredData() {
        filteredList.clear();

        for (Password p : passwordList) {
            if (matchesFilter(p)) {
                filteredList.add(p);
            }
        }
        reapplyTableSortOrder();
    }

    private boolean matchesFilter(Password password) {
        Category filterCategory = categoryBox.getSelectionModel().getSelectedItem();
        if (filterCategory == null) {
            return true;
        }
        return password.getCategoryObj().equals(filterCategory);
    }

    private void reapplyTableSortOrder() {
        ArrayList<TableColumn<Password, ?>> sortOrder = new ArrayList<>(tableView.getSortOrder());
        tableView.getSortOrder().clear();
        tableView.getSortOrder().addAll(sortOrder);
    }

    public void loadComboBoxData() {
         categoryBox.setItems(categoryList);
         comboBoxAP.setItems(categoryList);
         comboBoxRC.setItems(categoryList);
         categoryBox.getItems().add(null);
    }

    public static void saveToFile() throws IOException {
        List<Category> found = new ArrayList<>();
        for(Category category : categoryList){
            if(category == null){
                found.add(null);
            }
        }
        categoryList.removeAll(found);

        String originalString = passwordList.toString();
        String categoryString = categoryList.toString();

        List<String> linesP = Files.readAllLines(file.toPath());
        linesP.set(2, AES.encrypt(originalString, passwordListKey));
        Files.write(file.toPath(), linesP);

        List<String> linesC = Files.readAllLines(file.toPath());
        linesC.set(3, AES.encrypt(categoryString, categoryListKey));
        Files.write(file.toPath(), linesC);
    }

    public static String removeFirstAndLast(String str) {
        str = str.substring(1, str.length() - 1);

        return str;
    }

    @FXML
    public void addPButtonClick() {

        Dialog<Password> dialog = new Dialog<>();
        dialog.setTitle("Adding new password");

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Name");

        TextField password = new TextField();
        password.setPromptText("Password");


        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(name, 1, 0);

        gridPane.add(new Label("Password:"), 2, 0);
        gridPane.add(password, 3, 0);

        gridPane.add(new Label("Category:"), 4, 0);
        gridPane.add(comboBoxAP, 5, 0);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(name::requestFocus);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Password(name.getText(), password.getText(), comboBoxAP.getValue());
            }
            return null;
        });

        Optional<Password> result = dialog.showAndWait();

        result.ifPresent(p -> passwordList.add(p));
    }
    @FXML
    public void removePButtonClick() {
        Password selectedItem = tableView.getSelectionModel().getSelectedItem();
        passwordList.remove(selectedItem);
    }
    @FXML
    public void addCButtonClick() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Adding new category");

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Category name");

        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(name, 1, 0);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(name::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return name.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(p -> {
            Category c = new Category(p);
            categoryList.add(c);
        });
    }
    @FXML
    public void removeCButtonClick() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Removing category");

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        gridPane.add(new Label("Select category:"), 0, 0);
        gridPane.add(comboBoxRC, 1, 0);

        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(comboBoxRC::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Category(comboBoxRC.getValue().getCategory());
            }
            return null;
        });

        Optional<Category> result = dialog.showAndWait();

        result.ifPresent(p -> {
            boolean toBeRemoved = false;
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i) != null) {
                if (categoryList.get(i).getCategory().equals(p.getCategory())) {
                    //noinspection SuspiciousListRemoveInLoop
                        categoryList.remove(i);
                        toBeRemoved = true;
                    }
                }
            }
            if (toBeRemoved) {
                List<Password> found = new ArrayList<>();
                for(Password password : passwordList){
                    if(password.getCategoryObj().getCategory().equals(p.getCategory())){
                        found.add(password);
                    }
                }
                passwordList.removeAll(found);
                }
        });
    }
}
