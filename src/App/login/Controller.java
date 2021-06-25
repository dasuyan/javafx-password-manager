package App.login;

import App.main.AES;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Controller {

    @FXML
    public Button fileChooserButton;
    @FXML
    public Button loginButton;
    @FXML
    public Label fileName;
    @FXML
    public PasswordField masterPassword;
    @FXML
    public Label isPassCorrect;

    public static File file;
    public static String lastSuccessfulTS;
    private final String masterPasswordKey = "that's a master password";
    private final String timeStampKey = "that's a timestamp";

    public void initialize() {
    loginButton.setDisable(true);
    }

    @FXML
    public void fcButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./src/App/sourceFiles"));
        file = fileChooser.showOpenDialog(null);
        fileName.setText(file.getName());
        loginButton.setDisable(false);
    }

    @FXML
    public void loginButtonClick() {
        BufferedReader objReader = null;
        try {
            String line;
            objReader = new BufferedReader(new FileReader(file));
            line = objReader.readLine();

            //writeMP("pass");

            if (Objects.equals(AES.decrypt(line, masterPasswordKey), masterPassword.getText())) {
                isPassCorrect.setText("Correct password");
                readTimeStamp();
                writeTimestamp();
                objReader.close();

                changeScene();
            }
            else isPassCorrect.setText("Invalid password");

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

    public void writeTimestamp() throws IOException {
        Date date = new Date();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);
        String originalTS = ts.toString();

        List<String> lines = Files.readAllLines(file.toPath());
        lines.set(1, AES.encrypt(originalTS, timeStampKey));
        Files.write(file.toPath(), lines);
    }

    public void readTimeStamp() {
        BufferedReader objReader = null;
        try {
            String line = "no record";
            objReader = new BufferedReader(new FileReader(Controller.file));

            for (int i = 0; i < 2; i++)
                line = objReader.readLine();

            lastSuccessfulTS = AES.decrypt(line,timeStampKey);
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

    public void changeScene() throws IOException {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../main/main.fxml")));
        Scene scene = new Scene(root, 770, 500);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            try {
                App.main.Controller.saveToFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        stage.show();
    }

    //Generates main password, current password is "pass"
    public void writeMP(String mp) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        lines.set(0, AES.encrypt(mp, masterPasswordKey));
        Files.write(file.toPath(), lines);
    }
}