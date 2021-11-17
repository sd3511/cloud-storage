package gui;

import commonclasses.messages.CreateDirRequest;
import commonclasses.messages.DeleteRequest;
import commonclasses.messages.FolderRequest;
import commonclasses.messages.ListRequest;
import commonclasses.utils.FileInfo;
import connection.Network;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class CloudAreaController implements Initializable {
    public Button btnSend;
    public Button btnCut;
    public Button btnCopy;
    public Button btnPaste;
    public Button btnDownload;
    public Label currentPathClient;
    public Label currentPathServer;
    public ListView<FileInfo> clientListView;
    public ListView<FileInfo> serverListView;
    public ProgressBar progressBar;
    public Button btnUpClient;
    public Button btnUpServer;
    private Path clientDir;
    private Path currentDir;
    private Network network;
    private String login;
    private WatchService watchService;
    private WatchKey register;
    private double x;
    private double y;

    @SneakyThrows
    public void initialize(URL location, ResourceBundle resources) {
        createDir();
        currentDir = clientDir;
        setPropertiesServerView();
        setPropertiesClientListView();
        fileInfo(currentDir);
        watcherDir();
        register = currentDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        progressBar = new ProgressBar(0);

    }

    public void refreshServerList(ListRequest listRequest) {
        Platform.runLater(() -> {
            serverListView.getItems().clear();
            ObservableList<FileInfo> observableList = FXCollections.observableArrayList();
            if (listRequest.isHead()) {
                btnUpServer.setVisible(false);
            }
            observableList.addAll(listRequest.getFiles());
            serverListView.setItems(observableList);
        });


    }

    private void createDir() {
        clientDir = Paths.get("clientDir");
        if (!Files.exists(clientDir)) {
            try {
                Files.createDirectory(clientDir);
            } catch (IOException e) {
                log.debug("E = ", e);
            }
        }
    }

    public void sendFile() {
        List<String> collect = clientListView.getSelectionModel().getSelectedItems().stream().map(x -> x.getFilename()).collect(Collectors.toList());
        for (String s : collect) {
            Path path = Paths.get(currentDir.toString(), s);
            network.sendFile(path);
        }

    }

    public void download() {
        List<String> collect = serverListView.getSelectionModel().getSelectedItems().stream().map(x -> x.getFilename()).collect(Collectors.toList());
        for (String s : collect) {
            network.sendDownloadRequest(s);
        }
    }

    public void exitAction() {
        network.close();
        Platform.exit();
    }

    public Path getClientDir() {
        return clientDir;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPropertiesClientListView() {
        dragFilesToServer();
        clientListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clientListView.setCellFactory(c -> new FileInfoListCell());

        clientListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (clientListView.getSelectionModel().getSelectedItem().isFolder()) {
                    register.cancel();
                    currentDir = Paths.get(currentDir.toAbsolutePath().toString(), clientListView.getSelectionModel().getSelectedItem().getFilename());
                    try {
                        register = currentDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (currentDir.toAbsolutePath() != clientDir.toAbsolutePath()) {
                        btnUpClient.setVisible(true);
                    }
                    fileInfo(currentDir);
                } else {
                    Path path = Paths.get(currentDir.toAbsolutePath().toString(), clientListView.getSelectionModel().getSelectedItem().getFilename());

                    try {
                        Desktop.getDesktop().open(new File(path.toUri()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ContextMenu menu = createClientMenu();

        clientListView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                x = event.getSceneX();
                y = event.getSceneY();
                menu.show(clientListView, x, y);
            }
        });
    }

    public void setPropertiesServerView() {
        dragFilesFromServer();
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.setCellFactory(x -> new FileInfoListCell());
        serverListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (serverListView.getSelectionModel().getSelectedItem().isFolder()) {
                    network.send(new FolderRequest(serverListView.getSelectionModel().getSelectedItem().getFilename()));
                    btnUpServer.setVisible(true);
                }
            }
        });
        ContextMenu menu = createServerMenu();
        serverListView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                x = event.getSceneX();
                y = event.getSceneY();
                menu.show(serverListView, x, y);
            }
        });
    }

    private ContextMenu createClientMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Open");
        MenuItem menuItem2 = new MenuItem("Send");
        MenuItem menuItem3 = new MenuItem("Delete");
        MenuItem menuItem4 = new MenuItem("Create directory");
        menuItem1.setOnAction(event -> {
            Path path = Paths.get(currentDir.toAbsolutePath().toString(), clientListView.getSelectionModel().getSelectedItem().getFilename());
            try {
                Desktop.getDesktop().open(new File(path.toUri()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        menuItem3.setOnAction(event -> {
            try {
                Files.delete(Paths.get(currentDir.toAbsolutePath().toString(), clientListView.getSelectionModel().getSelectedItem().getFilename().toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        menuItem2.setOnAction(event -> sendFile());
        menuItem4.setOnAction(event -> {
            Stage newFolderStage = new Stage();
            TextField field = new TextField();

            newFolderStage.setScene(new Scene(field));
            //  newFolderStage.initStyle(StageStyle.UNDECORATED);
            newFolderStage.initModality(Modality.WINDOW_MODAL);
            newFolderStage.initOwner(Start.stage);
            field.setOnAction(event1 -> {
                try {
                    Files.createDirectory(Paths.get(currentDir.toAbsolutePath().toString(), field.getText()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                newFolderStage.close();
            });

            newFolderStage.setX(x);
            newFolderStage.setY(y);
            newFolderStage.show();
        });
        menu.getItems().addAll(menuItem1, menuItem2, menuItem3, menuItem4);
        return menu;
    }

    private ContextMenu createServerMenu() {
        ContextMenu menu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Download");
        MenuItem menuItem2 = new MenuItem("Delete");
        MenuItem menuItem3 = new MenuItem("Create directory");
        menuItem1.setOnAction(event -> {
            download();
        });

        menuItem2.setOnAction(event -> {
            List<String> collect = serverListView.getSelectionModel().getSelectedItems().stream().map(x -> x.getFilename()).collect(Collectors.toList());
            for (String s : collect) {
                network.send(new DeleteRequest(s));
            }
        });
        menuItem3.setOnAction(event -> {
            Stage newFolderStage = new Stage();
            TextField field = new TextField();

            newFolderStage.setScene(new Scene(field));
            //  newFolderStage.initStyle(StageStyle.UNDECORATED);
            newFolderStage.initModality(Modality.WINDOW_MODAL);
            newFolderStage.initOwner(Start.stage);
            field.setOnAction(event1 -> {
                network.send(new CreateDirRequest(field.getText()));
                newFolderStage.close();
            });

            newFolderStage.setX(x);
            newFolderStage.setY(y);
            newFolderStage.show();
        });
        menu.getItems().addAll(menuItem1, menuItem2, menuItem3);
        return menu;
    }

    @SneakyThrows
    public void fileInfo(Path dir) {
        ObservableList<FileInfo> observableList = FXCollections.observableArrayList();
        List<FileInfo> collect = Files.list(dir).map(FileInfo::new).collect(Collectors.toList());
        observableList.addAll(collect);
        Platform.runLater(() -> {
            clientListView.getItems().clear();
            clientListView.setItems(observableList);
        });


    }


    public void goToParentClient(ActionEvent actionEvent) {
        currentDir = currentDir.getParent().toAbsolutePath();
        if (currentDir.toAbsolutePath().equals(clientDir.toAbsolutePath())) {
            btnUpClient.setVisible(false);
        }
        fileInfo(currentDir);

    }

    public void goToParentServer(ActionEvent actionEvent) {
        network.send(new FolderRequest("/up"));
    }


    private void watcherDir() throws Exception {
        watchService = FileSystems.getDefault().newWatchService();
        Thread watchClient = new Thread(() -> {
            try {
                while (true) {
                    log.debug("Wait events...");
                    WatchKey watchKey = watchService.take();
                    List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                    for (WatchEvent<?> watchEvent : watchEvents) {
                        log.debug("1 - {}, 2- {}", watchEvent.context(), watchEvent.kind());
                    }
                    fileInfo(currentDir);
                    watchKey.reset();
                }
            } catch (Exception e) {
                log.debug("E=", e);
            }
        });
        watchClient.setDaemon(true);
        watchClient.start();

    }

    public Path getCurrentDir() {
        return currentDir;
    }

    private void dragFilesToServer() {
        clientListView.setOnDragDetected(event -> {
            Dragboard dragboard = clientListView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            List<String> selectFileName = new ArrayList<>();
            selectFileName.addAll(clientListView.getSelectionModel()
                    .getSelectedItems()
                    .stream()
                    .map(x -> x.getFilename())
                    .collect(Collectors.toList()));
            content.putFilesByPath(selectFileName);
            clientListView.setStyle("-fx-opacity: 1;");
            dragboard.setContent(content);

        });
        clientListView.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            clientListView.setStyle("-fx-opacity: 1;");
        });
        serverListView.setOnDragEntered(event -> {
            if (event.getGestureSource() != clientListView) {
                event.acceptTransferModes(TransferMode.NONE);
            }
            serverListView.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
        });
        serverListView.setOnDragOver(event -> {
            if (event.getGestureSource() != clientListView) {
                event.acceptTransferModes(TransferMode.NONE);
            } else {
                event.acceptTransferModes(TransferMode.COPY);
                serverListView.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
                clientListView.setStyle("-fx-opacity: 1;");
            }
        });
        serverListView.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            serverListView.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            clientListView.setStyle("-fx-opacity: 1;");
        });
        serverListView.setOnDragDropped(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            serverListView.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            clientListView.setStyle("-fx-opacity: 1;");
            sendFile();
        });
    }


    private void dragFilesFromServer() {
        serverListView.setOnDragDetected(event -> {
            Dragboard dragboard = serverListView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            List<String> selectFileName = new ArrayList<>();
            selectFileName.addAll(serverListView.getSelectionModel()
                    .getSelectedItems()
                    .stream()
                    .map(x -> x.getFilename())
                    .collect(Collectors.toList()));
            content.putFilesByPath(selectFileName);
            serverListView.setStyle("-fx-opacity: 1;");
            dragboard.setContent(content);

        });
        serverListView.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            serverListView.setStyle("-fx-opacity: 1;");
        });
        clientListView.setOnDragEntered(event -> {
            if (event.getGestureSource() != serverListView) {
                event.acceptTransferModes(TransferMode.NONE);
            }
            clientListView.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
        });
        clientListView.setOnDragOver(event -> {
            if (event.getGestureSource() != serverListView) {
                event.acceptTransferModes(TransferMode.NONE);
            } else {
                event.acceptTransferModes(TransferMode.COPY);
                clientListView.setStyle("-fx-opacity: 0.3; -fx-background-color: white;");
                serverListView.setStyle("-fx-opacity: 1;");
            }
        });
        clientListView.setOnDragExited(event -> {
            event.acceptTransferModes(TransferMode.NONE);
            clientListView.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            serverListView.setStyle("-fx-opacity: 1;");
        });
        clientListView.setOnDragDropped(event -> {
            event.acceptTransferModes(TransferMode.COPY);
            clientListView.setStyle("-fx-opacity: 1; -fx-background-color: white;");
            serverListView.setStyle("-fx-opacity: 1;");
            download();
        });
    }


}



