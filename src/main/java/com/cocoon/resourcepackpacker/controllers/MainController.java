package com.cocoon.resourcepackpacker.controllers;

import com.cocoon.resourcepackpacker.App;
import com.cocoon.resourcepackpacker.Asset;
import com.cocoon.resourcepackpacker.Cells.ResourcePathCell;
import com.cocoon.resourcepackpacker.Cells.StatusCell;
import com.cocoon.resourcepackpacker.config.Config;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static com.cocoon.resourcepackpacker.Asset.newAsset;

public class MainController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private ImageView imagePreviewer;

    @FXML
    private Label imagePreviewerLabel;

    @FXML
    private Label versioningLabel;

    @FXML
    private TreeTableView<Asset> treeTableView;

    @FXML
    private TreeTableColumn<Asset, String> resourceFileTree;
    @FXML
    private TreeTableColumn<Asset, Boolean> statusTree;

    private static final TreeItem<Asset> rootTreeItem = newAsset("assets", null);

    private static JarFile jarFile;

    private static String rootPath = null;

    private static Manifest manifest;

    public static Properties properties = new Properties();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createTreeTableView();
        // Load files based on config

        String currentLocation = null;
        try {
            URI uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            currentLocation = normalizePath(Paths.get(uri).getParent().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //currentLocation = URLDecoder.decode(currentLocation, StandardCharsets.UTF_8);

        System.out.println(currentLocation);
        assert currentLocation != null;
        Config.loadCustomProperties(Config.jarProperties, Path.of(currentLocation), new String[] {"referencePath","projectPath","imageEditorPath"});
        String referencePath = Config.jarProperties.getProperty("referencePath");
        String projectPath = Config.jarProperties.getProperty("projectPath");

        if (projectPath != null && !projectPath.equals("null") && Files.exists(Path.of(projectPath))) {
            System.out.println(projectPath);
            loadProject(Path.of(projectPath));
        }
        if (referencePath != null && !referencePath.equals("null") && Files.exists(Path.of(referencePath))) {
            System.out.println(referencePath);
            loadReference(referencePath);
        }
    }

    public void onSelectReferenceItem(ActionEvent event) {
        // Open the file select dialog
        File selectedFile = openFileDialog("Jar Files...", "*.jar");
        // If result is null, ignore the rest
        if (selectedFile == null) {return;}

        createTreeTableView();

        // Get the path of the file
        String filepath = normalizePath(selectedFile.getAbsolutePath());
        loadReference(filepath);

        // Save the root path to jar config
        Config.jarProperties.setProperty("referencePath", filepath);
        Config.save(Config.jarProperties, 1);
    }

    public void onSelectProjectItem(ActionEvent event) {
        // Open the file select dialog
        File selectedFile = openFileDialog("Mcmeta Files...", "*.mcmeta");
        // If result is null, ignore the rest
        if (selectedFile == null) {return;}

        createTreeTableView();
        // Scan for every .png file in the path, and set the status of corresponding files in the treeview
        rootPath = normalizePath(selectedFile.getParentFile().toPath().toString());

        loadProject(Paths.get(rootPath));
        // Save the root path to jar config
        Config.jarProperties.setProperty("projectPath", rootPath+"/");
        Config.save(Config.jarProperties, 1);
    }

    public void onSelectImageEditorItem(ActionEvent event) {
        // Open the file select dialog
        File selectedFile = openFileDialog("Select Executable...", null);
        // If result is null, ignore the rest
        if (selectedFile == null) {return;}

        // Save the path to jar config
        Config.jarProperties.setProperty("imageEditorPath", normalizePath(selectedFile.getPath()));
        Config.save(Config.jarProperties, 1);
    }

    private void loadReference(String filepath) {
        // Magic from https://www.devx.com/tips/java/reading-contents-of-a-jar-file-using-java.-170629013043.html
        try{
            // Load the jar file using the filepath above
            if (filepath == null || filepath.equals("null")) {return;}
            jarFile = new JarFile(filepath);

            Enumeration<JarEntry> enumOfJar = jarFile.entries();
            while (enumOfJar.hasMoreElements()) {
                JarEntry file = enumOfJar.nextElement();
                String filename = file.getName();
                // If the file is not a png under assets, simply ignore it.
                if (filename.startsWith("assets/") && filename.endsWith(".png")) {
                    generateTreeTableViewContent(filename, filename, rootTreeItem);
                }
            }
            sortTree(resourceFileTree, TreeTableColumn.SortType.ASCENDING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProject(Path path) {
        rootPath = normalizePath(path.toString());
        //Create/Load the config
        Config.loadCustomProperties(Config.projectProperties, path, new String[] {"completed"});

        reloadProject();

        // Unlock treetableview to allow editing
        treeTableView.setDisable(false);
    }

    private File openFileDialog(String text, String types, Path defaultPath) {
        // Create a new filechooser
        FileChooser fileChooser = new FileChooser();
        if (defaultPath != null) {
            fileChooser.setInitialDirectory(defaultPath.toFile());
        }
        // Set the filter
        if (types != null) {
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(text, types));
        }
        // Open the file select dialog
        return fileChooser.showOpenDialog(null);
    }

    private File openFileDialog(String text, String types) {
        return openFileDialog(text, types, null);
    }

    private void createTreeTableView() {
        clearTreeTableView();
        resourceFileTree.setCellFactory(p -> {
            TreeTableCell<Asset, String> cell = new ResourcePathCell();
            cell.setOnMouseClicked(event -> onCellClicked(cell, event));
            return cell ;
        });
        resourceFileTree.setCellValueFactory(new TreeItemPropertyValueFactory<>("file"));
        statusTree.setCellValueFactory(new TreeItemPropertyValueFactory<>("status"));
//        statusTree.setCellValueFactory(value -> {
//            System.out.println("hi");
//            return value.getTreeTableColumn().getCellObservableValue(3);
//        });
        statusTree.setCellFactory(p -> new StatusCell());
        setTreeTableViewRoot(rootTreeItem);
        setTreeTableViewColumns();
    }

    // Generate the main tree
    private static void generateTreeTableViewContent(String originalPath, String path, TreeItem<Asset> currentLevel) {
        // If the file exists in project, set status to false (imported)
        File projectFile = new File(rootPath + "/" + originalPath);
        if (projectFile.exists()) {
            currentLevel.getValue().setStatus(false);
        }
        // Split string into segments
        String[] pathSegments = path.split("/");
        // If the first element of the path matches, it has been generated, so loop with the next element
        if (Objects.equals(pathSegments[0], currentLevel.getValue().getFile())) {
            // Reconstruct the string without the first element
            String new_path = truncatePathString(pathSegments, currentLevel.getValue().getFile());
            // If this is the last element, return
            if (pathSegments.length <= 1) {return;}
            // Else, find the next item in the children
            TreeItem<Asset> nextLevel = null;
            for (TreeItem<Asset> t: currentLevel.getChildren()) {
                if (Objects.equals(t.getValue().getFile(), pathSegments[1])) {
                    nextLevel = t;
                }
            }
            // If can't find the next item, loop with the next item
            if (Objects.equals(nextLevel, null)) {
                TreeItem<Asset> newChild = newAsset(pathSegments[1], null, originalPath);
                currentLevel.getChildren().add(newChild);
                generateTreeTableViewContent(originalPath, new_path, newChild);
            } // If the item is found, loop with that as the next layer
            else {
                generateTreeTableViewContent(originalPath, new_path, nextLevel);
            }
        } // If not, generate it
        else {
            currentLevel.getChildren().add(newAsset(pathSegments[0], null, originalPath));
        }
    }

    // Update Tree Table View by reloading resource pack
    private void reloadProject() {
        ArrayList<String> paths = listFiles(rootPath);
        if (paths == null) {return;}
        for (String s : paths) {
            // Remove the front part so only /assets/ is left
            String path = s.replaceFirst(rootPath.toString() + "/", "");
            if (path.startsWith("assets/") && path.endsWith(".png")) {
                generateTreeTableViewContent(path, path, rootTreeItem);
            }
        }
        sortTree(resourceFileTree, TreeTableColumn.SortType.ASCENDING);
    }

    private static String truncatePathString(String[] pathSegments, String currentLevelString) {
        // Reconstruct the string without the first element
        StringBuilder new_path = new StringBuilder();
        for (String s : pathSegments) {
            if (!(Objects.equals(s, currentLevelString))) {
                new_path.append(s);
                // If this is not the last, add /
                if (!s.endsWith(".png")) {
                    new_path.append("/");
                }
            }
        }
        return new_path.toString();
    }

    private void setTreeTableViewRoot(TreeItem<Asset> root) {
        treeTableView.setRoot(root);
    }

    private void clearTreeTableView() {
        treeTableView.getColumns().remove(resourceFileTree);
        treeTableView.getColumns().remove(statusTree);
    }

    private void setTreeTableViewColumns() {
        treeTableView.getColumns().add(resourceFileTree);
        treeTableView.getColumns().add(statusTree);
        sortTree(resourceFileTree, TreeTableColumn.SortType.ASCENDING);
    }

    private void sortTree(TreeTableColumn<Asset, ?> ttc, TreeTableColumn.SortType sortType) {
        ttc.setSortType(sortType);
        treeTableView.getSortOrder().add(ttc);
    }

    private void setThumbnail(String path) {
        // Try to get the file using rootPath + path first
        // If it does not exist, get the texture inside the Jar
        String labelText = path;
        File projectFile = new File(rootPath + "/" + path);
        FileInputStream fileIS;
        BufferedInputStream bufferedIS;
        try {
            if (projectFile.exists()) {
                fileIS = new FileInputStream(projectFile);
                bufferedIS = new BufferedInputStream(fileIS);
            } else {
                JarEntry file = jarFile.getJarEntry(path);
                bufferedIS = new BufferedInputStream(jarFile.getInputStream(file));
                labelText = file.getName();
            }
            Image image = new Image(bufferedIS, 400, 400, true, false);
            imagePreviewer.setImage(image);
            imagePreviewerLabel.setText(labelText);
            bufferedIS.close();
        } catch (IOException ignored) {}
    }

    private void onCellClicked(TreeTableCell<Asset, String> cell, MouseEvent event) {
        if (cell == null || cell.getTreeTableRow().getTreeItem() == null) {return;}
        TreeItem<Asset> currentTreeItem = cell.getTreeTableRow().getTreeItem();
        String path = currentTreeItem.getValue().getPath();
        String file = currentTreeItem.getValue().getFile();
        // If the file is a directory (i.e. not a png file), expand/retract it and stop
        if ( file == null ) {return;}
        if (!file.endsWith(".png")) {
            //if (event.getTarget().getClass().getTypeName().equals("com.sun.javafx.scene.control.LabeledText")) {
                currentTreeItem.setExpanded(!currentTreeItem.isExpanded());
            //}
            return;
        }
        // Open if its double-clicked
        if (event.getClickCount() == 2) {
            if (jarFile != null) {
                importToProject(cell, path);
            }
            String filePath = rootPath + "/" + path;
            String executeFilePath = Config.jarProperties.getProperty("imageEditorPath");
            if (executeFilePath == null || executeFilePath.equals("null")) {return;}
            try {
                Runtime runTime = Runtime.getRuntime();
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    // WINDOWS (Didn't test this tbh, please let me know if it doesn't work)
                    runTime.exec(new String[] {"start /b", executeFilePath, filePath});
                } else {
                    // LINUX/MAC? Linux works for sure
                    runTime.exec(new String[]{"nohup", executeFilePath, filePath});
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setThumbnail(path);
    }

    // list all files from this path, return as string. To get real path, rootPath + path
    private static ArrayList<String> listFiles(String path) {
        if (path == null) {
            return null;
        }

        ArrayList<String> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            walk.toList().forEach(e -> result.add(e.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    private void importToProject(TreeTableCell<Asset, String> cell, String path) {
        try {
            String targetPath = rootPath+"/"+path;
            File targetFile = new File(targetPath);
            targetFile.getParentFile().mkdirs();
            if (!targetFile.exists()) {
                JarEntry file = jarFile.getJarEntry(path);

                BufferedInputStream bufferedIS = new BufferedInputStream(jarFile.getInputStream(file));

                Files.copy(bufferedIS, targetFile.toPath());
                bufferedIS.close();
                reloadProject();
                // TODO: Update status display
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String normalizePath(String path) {
        return path.replace("\\", "/");
    }
}