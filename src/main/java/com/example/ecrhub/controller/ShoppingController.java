package com.example.ecrhub.controller;

import com.example.ecrhub.manager.ECRHubClientManager;
import com.example.ecrhub.manager.PurchaseManager;
import com.example.ecrhub.manager.SceneManager;
import com.example.ecrhub.pojo.ECRHubClientPo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;

/**
 * @author: yanzx
 * @date: 2023/10/13 15:59
 * @description:
 */
public class ShoppingController {

    public BorderPane container;

    public GridPane productGrid;

    public Label amount;

    public Button queryResponse;
    public Button refundResponse;

    @FXML
    ListView<String> listView;

    public void initialize() {
        PurchaseManager.getInstance().setTrans_amount(null);
        // 模拟商品数据，包含商品名称和价格
        String[][] products = {
                {"Beer", "1.00"},
                {"Coffee", "8.80"},
                {"Hamburg", "10.00"},
                {"Waffle", "0.01"},
                {"Milk", "3.50"},
                {"Pasta", "20.00"},
                {"Pudding", "3.33"},
                {"Cocktail", "12.00"},
                {"Steak", "15.00"}
        };

        int col = 0;
        int row = 0;

        for (String[] product : products) {
            String productName = product[0];
            String productPrice = product[1];

            // 商品图标，替换成实际商品图片的路径
            Image productImage = new Image(ShoppingController.class.getResourceAsStream("/com/example/ecrhub/image/" + productName + ".png"));
            ImageView imageView = new ImageView(productImage);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);

            // 商品名称和价格
            Label nameLabel = new Label(productName);
            Label priceLabel = new Label("$" + productPrice);

            // 包含商品图标、名称和价格的 VBox
            VBox productBox = new VBox(5);
            productBox.setAlignment(Pos.CENTER);
            Insets productInsets = new Insets(0, 0, 0, 30);
            productBox.setPadding(productInsets);

            HBox priceBox = new HBox(5);
            priceBox.getChildren().addAll(nameLabel, priceLabel);

            // 商品数量
            Label numLabel = new Label("x ");
            TextField numField = createNumericField();
            numField.setText("1");
            numField.setMaxWidth(50);
            numField.setAlignment(Pos.CENTER);

            HBox numBox = new HBox(5);
            numBox.getChildren().addAll(numLabel, numField);
            numBox.setAlignment(Pos.CENTER_RIGHT);

            productBox.getChildren().addAll(imageView, priceBox, numBox);

            // 商品点击事件
            productBox.setOnMouseClicked(event -> addToCart(productName, productPrice, numField.getText()));

            productGrid.add(productBox, col, row);

            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }

    private void addToCart(String productName, String productPrice, String productNum) {
        String item = productName + " - $" + productPrice + " x " + productNum;
        listView.getItems().add(item);

        String amount_str = amount.getText().replace("$", "");
        BigDecimal total_amount = new BigDecimal(amount_str).add((new BigDecimal(productPrice).multiply(new BigDecimal(productNum))));
        amount.setText("$" + total_amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        PurchaseManager.getInstance().setTrans_amount(amount);
    }

    @FXML
    private void handleReturnButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("home", "/com/example/ecrhub/fxml/home.fxml");
        SceneManager.getInstance().switchScene("home");
    }

    @FXML
    private void handleRefundButtonAction(ActionEvent event) {
        SceneManager.getInstance().loadScene("refundResponse", "/com/example/ecrhub/fxml/refundResponse.fxml");
        SceneManager.getInstance().switchScene("refundResponse");
    }

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {

        SceneManager.getInstance().loadScene("queryResponse", "/com/example/ecrhub/fxml/queryResponse.fxml");
        SceneManager.getInstance().switchScene("queryResponse");
    }

    @FXML
    private void handleNextButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        if ("$0".equals(amount.getText())) {
            alert.setContentText("Please select product!");
            alert.showAndWait();
            return;
        }

        if (0 == ECRHubClientManager.getInstance().getConnectType()) {
            alert.setContentText("Please connect the device!");
            alert.showAndWait();
            return;
        } else if (2 == ECRHubClientManager.getInstance().getConnectType()) {
            boolean have_connected = false;
            LinkedHashMap<String, ECRHubClientPo> client_list = ECRHubClientManager.getInstance().getClient_list();
            if (!client_list.isEmpty()) {
                for (String key : client_list.keySet()) {
                    ECRHubClientPo client_info = client_list.get(key);
                    if (client_info.isIs_connected()) {
                        have_connected = true;
                        break;
                    }
                }
                if (!have_connected) {
                    alert.setContentText("Device is not connected!");
                    alert.showAndWait();
                    return;
                }
            }
        }

        SceneManager.getInstance().loadScene("submit", "/com/example/ecrhub/fxml/submit.fxml");
        SceneManager.getInstance().switchScene("submit");
    }

    private TextField createNumericField() {
        TextField textField = new TextField();

        // 添加事件过滤器，只允许输入数字
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String character = event.getCharacter();
            if (!character.matches("[0-9]")) {
                event.consume(); // 阻止非数字字符的输入
            }
        });

        return textField;
    }
}
