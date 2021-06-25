package App.main;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;

import static App.main.Controller.categoryList;

public class ComboBoxEditingCell extends TableCell<Password, Category> {

    public ComboBox<Category> comboBox;

    public ComboBoxEditingCell() {
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createComboBox();
            setText(null);
            setGraphic(comboBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getCategory().getCategory());
        setGraphic(null);
    }

    @Override
    public void updateItem(Category item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                    comboBox.setValue(getCategory());
                }
                setText(getCategory().getCategory());
                setGraphic(comboBox);
            } else {
                setText(getCategory().getCategory());
                setGraphic(null);
            }
        }
    }

    public void createComboBox() {
        comboBox = new ComboBox<>(categoryList);
        comboBoxConverter(comboBox);
        comboBox.valueProperty().set(getCategory());
        comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        comboBox.setOnAction((e) -> commitEdit(comboBox.getSelectionModel().getSelectedItem()));
    }

    public void comboBoxConverter(ComboBox<Category> comboBox) {
        comboBox.setCellFactory((c) -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getCategory());
                }
            }
        });
    }

    public Category getCategory() {
        return getItem() == null ? new Category("") : getItem();
    }
}
