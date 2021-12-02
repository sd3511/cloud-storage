package gui;

import commonclasses.utils.FileInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

public class FileInfoListCell extends ListCell<FileInfo>  {


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
                mLLoader = new FXMLLoader(getClass().getResource("cell.fxml"));
                mLLoader.setController(this);
                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String str;
            if (item.getSize() / 1073741824 > 0) {
                double a = ((double)item.getSize()/1073741824);
                 str = new DecimalFormat("#0.00").format(a);
                fileSize.setText(str + " GB");
            } else if (item.getSize() / 1048576 > 0) {
                double a = ((double)item.getSize()/1048576);
                 str = new DecimalFormat("#0.00").format(a);
                fileSize.setText(str + " MB");
            } else if (item.getSize() / 1024 > 0) {
                double a = ((double)item.getSize()/1024);
                 str = new DecimalFormat("#0.00").format(a);
                fileSize.setText(str + " KB");
            } else if (item.getSize() / 1024 <= 0) {

                fileSize.setText(item.getSize() + " bytes");
            }
            checkItem.setSelected(item.isChosen());
            checkItem.setDisable(true);
            fileName.setText(String.valueOf(item.getFilename()));


            InputStream is = getClass().getResourceAsStream("/image/null.png");
            String extension = FilenameUtils.getExtension(item.getFilename());
            boolean isFolder = item.isFolder();
            if (isFolder) {
                is = getClass().getResourceAsStream("/image/folder.png");
            } else {
                if (extension.equalsIgnoreCase("txt")) {
                    is = getClass().getResourceAsStream("/image/txt.png");
                } else if (extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpg")) {
                    is = getClass().getResourceAsStream("/image/picture.png");
                } else if (extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("mkv")) {
                    is = getClass().getResourceAsStream("/image/vide.png");
                } else if (extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("doc")) {
                    is = getClass().getResourceAsStream("/image/word.png");
                }
            }

            ImageView imageView = new ImageView(new Image(is, 32, 32, false, false));

            fileIcon.setGraphic(imageView);

            setText(null);
            setGraphic(hBox);
        }

    }


    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
        if (selected) {
            checkItem.setSelected(true);
        } else {
            checkItem.setSelected(false);
        }
    }



}

