package com.cocoon.resourcepackpacker;

import com.cocoon.resourcepackpacker.config.Config;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;

public class Cells {
    public static class ResourcePathCell extends TreeTableCell<Asset, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            // If the cell is empty we don't show anything.
            if (isEmpty()) {
                setGraphic(null);
                setText(null);
            } else {
                // If the cell is not empty, set the text
                setText(item);
            }

        }
    }
    public static class StatusCell extends TreeTableCell<Asset, Boolean> {
        String[] styleTexts = {"default","imported","completed"};
        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            TreeItem<Asset> treeItem = this.getTreeTableRow().getTreeItem();
            // If the cell is empty we don't show anything.
            if (isEmpty() || treeItem == null || !treeItem.getValue().getFile().endsWith(".png")) {
                setGraphic(null);
                setText(null);
                this.setStyle("-fx-background-color: -background-color");
            } else {
                // If the cell is not empty, create a tick box in it
                // Unless the row is not an item
                Asset treeItemValue = treeItem.getValue();
                CheckBox checkBox = new CheckBox();
                checkBox.setAlignment(Pos.CENTER);
                if (treeItemValue.getStatus() == null) {
                    checkBox.setDisable(true);
                }
                checkBox.setOnMouseClicked(event -> {
                    boolean isCheckBoxSelected = checkBox.isSelected();
                    // If the status is null when pressed, it means it was in default,
                    // So set the value to false (imported)
                    // TODO: Replace with import
                    if (treeItemValue.getStatus() == null) {
                        isCheckBoxSelected = false;
                        checkBox.setSelected(false);
                        checkBox.setDisable(false);
                    }
                    checkBox.setAllowIndeterminate(false);
                    if (isCheckBoxSelected) {
                        Config.arrayPropertyAdd(Config.projectProperties, "completed", treeItemValue.getPath());
                    } else {
                        Config.arrayPropertyRemove(Config.projectProperties, "completed", treeItemValue.getPath());
                    }
                    treeItemValue.setStatus(isCheckBoxSelected);
                    setCellDisplay(isCheckBoxSelected, checkBox);
                });
                Boolean isPathCompleted = Config.isInArrayProperty(Config.projectProperties, "completed", treeItemValue.getPath());
                if (item == null || isPathCompleted == null) {
                    setCellDisplay(null, checkBox);
                } else {
                    setCellDisplay((isPathCompleted || item), checkBox);
                }
            }
        }
        private void setCellDisplay(Boolean bool, CheckBox checkBox) {
            String styleText = styleTexts[boolToInt(bool)];
            if (bool == null) {bool = false;}
            checkBox.setSelected(bool);
            setStyle("-fx-background-color: -" + styleText + "-label-color");
            setText(styleText);
            setAlignment(Pos.CENTER);
            setGraphic(checkBox);
        }
    }

    private static int boolToInt(Boolean bool) {
        if (bool != null) {
            return bool ? 2 : 1;
        } else {
            return 0;
        }
    }
}