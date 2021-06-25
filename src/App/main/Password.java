package App.main;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Password {

        private final SimpleStringProperty name;
        private final SimpleStringProperty password;
        private final SimpleObjectProperty<Category> category;

        public Password(String name, String password, Category category) {
            this.name = new SimpleStringProperty(name);
            this.password = new SimpleStringProperty(password);
            this.category = new SimpleObjectProperty(category);
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return this.name;
        }

        public void setName(String name) {
            this.name.set(name);
        }


        public String getPassword() { return password.get();}

        public StringProperty passwordProperty() {
            return this.password;
        }

        public void setPassword(String name) {
        this.name.set(name);
        }


        public Category getCategoryObj() {
            return category.get();
        }

        public ObjectProperty<Category> categoryObjProperty() {
            return this.category;
        }

        public void setCategoryObj(Category typ) {
            this.category.set(typ);
        }

    @Override
    public String toString() {
        return name.getValue() + ":" + password.getValue() + ":" + category.getValue();
    }
}

