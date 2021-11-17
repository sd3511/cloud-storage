package gui;

import commonclasses.utils.FileInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.io.InputStream;

public class ListCellFile extends ListCell<FileInfo> {

    public HBox hBox;
    public CheckBox checkItem;
    public Label fileIcon;
    public Label fileName;
    public Label fileSize;
    FXMLLoader mLLoader;

    @Override
    protected void updateItem(FileInfo item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {

            setText(null);
            setGraphic(null);
        } else {

            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource("/cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            fileSize.setText(String.valueOf(item.getSize()));
            fileName.setText(String.valueOf(item.getFilename()));

            InputStream is = getClass().getResourceAsStream("image/null.png");
            ImageView imageView = new ImageView(new Image(is, 32, 32, false, false));

            fileIcon.setGraphic(imageView);



            setText(null);
            setGraphic(hBox);
        }

    }
}
