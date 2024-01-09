//package com.example.ecrhub.controller;
//
//import cn.hutool.core.util.HexUtil;
//import cn.hutool.core.util.IdUtil;
//import cn.hutool.core.util.StrUtil;
//import com.codepay.register.sdk.ECRHubClient;
//import com.codepay.register.sdk.ECRHubSerialPortClient;
//import com.codepay.register.sdk.protobuf.ECRHubRequestProto;
//import com.codepay.register.sdk.sp.serialport.SerialPortMessage;
//import com.example.ecrhub.manager.ECRHubClientManager;
//import com.example.ecrhub.manager.SceneManager;
//import com.example.ecrhub.pojo.ECRDebugPo;
//import javafx.application.Platform;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.concurrent.Task;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//
///**
// * @author: yanzx
// * @date: 2023/11/7 14:08
// * @description:
// */
//public class DebugController {
//
//    Logger logger = LoggerFactory.getLogger(DebugController.class);
//
//    public ChoiceBox<String> trans_choice;
//
//    @FXML
//    private RadioButton send_raw;
//
//    @FXML
//    private RadioButton send_pretty;
//
//    @FXML
//    private RadioButton receive_raw;
//
//    @FXML
//    private RadioButton receive_pretty;
//
//    @FXML
//    private HBox orig_merchant_order_no_box;
//
//    @FXML
//    private HBox order_amount_box;
//
//    @FXML
//    private HBox cashback_amount_box;
//
//    @FXML
//    private HBox pay_method_category_box;
//
//    @FXML
//    private TextField merchant_order_no;
//
//    @FXML
//    private TextField orig_merchant_order_no;
//
//    @FXML
//    private TextField order_amount;
//
//    @FXML
//    private TextField cashback_amount;
//
//    public ChoiceBox<String> pay_method_category_choice;
//
//    @FXML
//    private TextArea send_message;
//
//    @FXML
//    private Button create_message_button;
//
//    @FXML
//    private Button send_message_button;
//
//    @FXML
//    private TextArea receive_message;
//
//    private static String REQUEST_ID;
//
//    private ECRDebugPo DEBUG_PO;
//
//    @FXML
//    private VBox wait_vbox;
//
//    private Task<String> send_task = null;
//
//    private byte message_id;
//
//    public void initialize() {
//        DEBUG_PO = new ECRDebugPo();
//
//        trans_choice.getItems().addAll("Sale", "Cancel", "Query", "Close", "Pre-authorization", "Pre-auth Cancel", "Pre-auth Completion", "Pre-auth Completion Cancel", "Cash back");
//        trans_choice.setValue("Sale");
//
//        pay_method_category_choice.getItems().addAll("SWIPE_CARD", "SCANQR_PAY", "BSCANQR_PAY");
//        pay_method_category_choice.setValue("SWIPE_CARD");
//
//        ToggleGroup send_group = new ToggleGroup();
//        send_raw.setToggleGroup(send_group);
//        send_pretty.setToggleGroup(send_group);
//
//        ToggleGroup receive_group = new ToggleGroup();
//        receive_raw.setToggleGroup(receive_group);
//        receive_pretty.setToggleGroup(receive_group);
//
//        order_amount.focusedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                // 数据改变
//                if (oldValue && !newValue) {
//                    // 添加事件
//                    String amount_str = order_amount.getText();
//                    if (StrUtil.isNotEmpty(amount_str)) {
//                        BigDecimal amount;
//                        try {
//                            amount = new BigDecimal(amount_str);
//                            order_amount.setText(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
//                        } catch (Exception e) {
//                            Alert alert = new Alert(Alert.AlertType.ERROR);
//                            alert.setTitle("ERROR!");
//                            alert.setContentText("Please enter the correct order amount");
//                            alert.showAndWait();
//                            order_amount.setText(null);
//                        }
//                    }
//                }
//            }
//        });
//
//        cashback_amount.focusedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                // 数据改变
//                if (oldValue && !newValue) {
//                    // 添加事件
//                    String amount_str = cashback_amount.getText();
//                    if (StrUtil.isNotEmpty(amount_str)) {
//                        BigDecimal amount;
//                        try {
//                            amount = new BigDecimal(amount_str);
//                            cashback_amount.setText(amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
//                        } catch (Exception e) {
//                            Alert alert = new Alert(Alert.AlertType.ERROR);
//                            alert.setTitle("ERROR!");
//                            alert.setContentText("Please enter the correct cashback amount");
//                            alert.showAndWait();
//                            cashback_amount.setText(null);
//                        }
//                    }
//                }
//            }
//        });
//
//        send_message.focusedProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                // 数据改变
//                if (oldValue && !newValue) {
//                    String hex_string = send_message.getText();
//                    if (send_raw.isSelected() && StrUtil.isNotEmpty(hex_string)) {
//                        try {
//                            byte[] send_message_bytes = HexUtil.decodeHex(hex_string);
//                            SerialPortMessage portMessage = new SerialPortMessage().decode(send_message_bytes);
//                            byte[] message_bytes = portMessage.getMessageData();
//                            ECRHubRequestProto.ECRHubRequest hubRequest = ECRHubRequestProto.ECRHubRequest.parseFrom(message_bytes);
//                            REQUEST_ID = hubRequest.getRequestId();
//
//                            DEBUG_PO.setSend_raw(hex_string);
//                            DEBUG_PO.setSend_pretty(new SerialPortMessage().decode(send_message_bytes).toString());
//                        } catch (Exception e) {
//                            Alert alert = new Alert(Alert.AlertType.ERROR);
//                            alert.setTitle("ERROR!");
//                            alert.setContentText("Please enter the correct HEX Message!");
//                            alert.showAndWait();
//                            send_message.setText(null);
//                        }
//                    }
//                }
//            }
//        });
//
//        send_group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
//            RadioButton selectedRadioButton = (RadioButton) newValue;
//            String send_choose = selectedRadioButton.getText();
//            if ("Raw".equals(send_choose)) {
//                send_message.setEditable(true);
//                send_message.setText(DEBUG_PO.getSend_raw());
//            } else {
//                send_message.setEditable(false);
//                send_message.setText(DEBUG_PO.getSend_pretty());
//            }
//        });
//
//        receive_group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
//            RadioButton selectedRadioButton = (RadioButton) newValue;
//            String receive_choose = selectedRadioButton.getText();
//            if ("Raw".equals(receive_choose)) {
//                receive_message.setText(DEBUG_PO.getReceive_raw());
//            } else {
//                receive_message.setText(DEBUG_PO.getReceive_pretty());
//            }
//        });
//    }
//
//    @FXML
//    protected void onTransChoiceChange() {
//        // 初始化
//        DEBUG_PO = new ECRDebugPo();
//        send_raw.setSelected(true);
//        receive_raw.setSelected(true);
//        merchant_order_no.setText(null);
//        orig_merchant_order_no.setText(null);
//        order_amount.setText(null);
//        cashback_amount.setText(null);
//        send_message.setText(null);
//        receive_message.setText(null);
//        REQUEST_ID = null;
//        switch (trans_choice.getValue()) {
//            case "Pre-authorization":
//            case "Sale": {
//                orig_merchant_order_no_box.setVisible(false);
//                orig_merchant_order_no_box.setManaged(false);
//                order_amount_box.setVisible(true);
//                order_amount_box.setManaged(true);
//                cashback_amount_box.setVisible(false);
//                cashback_amount_box.setManaged(false);
//                pay_method_category_box.setVisible(true);
//                pay_method_category_box.setManaged(true);
//            }
//            return;
//            case "Pre-auth Cancel":
//            case "Pre-auth Completion Cancel":
//            case "Cancel": {
//                orig_merchant_order_no_box.setVisible(true);
//                orig_merchant_order_no_box.setManaged(true);
//                order_amount_box.setVisible(false);
//                order_amount_box.setManaged(false);
//                cashback_amount_box.setVisible(false);
//                cashback_amount_box.setManaged(false);
//                pay_method_category_box.setVisible(false);
//                pay_method_category_box.setManaged(false);
//            }
//            return;
//            case "Query":
//            case "Close": {
//                orig_merchant_order_no_box.setVisible(false);
//                orig_merchant_order_no_box.setManaged(false);
//                order_amount_box.setVisible(false);
//                order_amount_box.setManaged(false);
//                cashback_amount_box.setVisible(false);
//                cashback_amount_box.setManaged(false);
//                pay_method_category_box.setVisible(false);
//                pay_method_category_box.setManaged(false);
//            }
//            return;
//            case "Pre-auth Completion": {
//                orig_merchant_order_no_box.setVisible(true);
//                orig_merchant_order_no_box.setManaged(true);
//                order_amount_box.setVisible(true);
//                order_amount_box.setManaged(true);
//                cashback_amount_box.setVisible(false);
//                cashback_amount_box.setManaged(false);
//                pay_method_category_box.setVisible(false);
//                pay_method_category_box.setManaged(false);
//            }
//            return;
//            case "Cash back": {
//                orig_merchant_order_no_box.setVisible(false);
//                orig_merchant_order_no_box.setManaged(false);
//                order_amount_box.setVisible(true);
//                order_amount_box.setManaged(true);
//                cashback_amount_box.setVisible(true);
//                cashback_amount_box.setManaged(true);
//                pay_method_category_box.setVisible(true);
//                pay_method_category_box.setManaged(true);
//            }
//        }
//    }
//
//    @FXML
//    private void createSampleMessageAction(ActionEvent event) {
//        String topic = checkAndGetTopic();
//        if ("error".equals(topic)) {
//            return;
//        }
//        ECRHubRequestProto.RequestBizData.Builder builder = ECRHubRequestProto.RequestBizData.newBuilder();
//        builder.setMerchantOrderNo(merchant_order_no.getText());
//        switch (trans_choice.getValue()) {
//            case "Sale":
//                builder.setPayMethodCategory(pay_method_category_choice.getValue()).setTransType("1").setOrderAmount(order_amount.getText());
//                if ("SCANQR_PAY".equals(pay_method_category_choice.getValue())) {
//                    builder.setPayMethodId("Alipay");
//                }
//                break;
//            case "Cancel":
//                builder.setOrigMerchantOrderNo(orig_merchant_order_no.getText()).setTransType("2");
//                break;
//            case "Pre-authorization":
//                builder.setPayMethodCategory(pay_method_category_choice.getValue()).setTransType("4").setOrderAmount(order_amount.getText());
//                break;
//            case "Pre-auth Cancel":
//                builder.setOrigMerchantOrderNo(orig_merchant_order_no.getText()).setTransType("5");
//                break;
//            case "Pre-auth Completion":
//                builder.setOrigMerchantOrderNo(orig_merchant_order_no.getText()).setTransType("6").setOrderAmount(order_amount.getText());
//                break;
//            case "Pre-auth Completion Cancel":
//                builder.setOrigMerchantOrderNo(orig_merchant_order_no.getText()).setTransType("7");
//                break;
//            case "Cash back":
//                builder.setPayMethodCategory(pay_method_category_choice.getValue()).setTransType("11")
//                        .setOrderAmount(order_amount.getText()).setCashbackAmount(cashback_amount.getText());
//        }
//
//        REQUEST_ID = IdUtil.fastSimpleUUID();
//        ECRHubRequestProto.RequestBizData bizData = builder.build();
//        ECRHubRequestProto.ECRHubRequest request = ECRHubRequestProto.ECRHubRequest.newBuilder()
//                .setRequestId(REQUEST_ID)
//                .setTimestamp(String.valueOf(System.currentTimeMillis()))
//                .setTopic(topic)
//                .setBizData(bizData)
//                .build();
//        byte[] message = new SerialPortMessage.DataMessage(request.toByteArray()).encode();
//        DEBUG_PO.setSend_raw(HexUtil.encodeHexStr(message, false));
//        DEBUG_PO.setSend_pretty(new SerialPortMessage().decode(message).toString());
//
//        send_message.setText(send_raw.isSelected() ? DEBUG_PO.getSend_raw() : DEBUG_PO.getSend_pretty());
//        send_message_button.setDisable(false);
//    }
//
//    private String checkAndGetTopic() {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle("ERROR!");
//        if (StrUtil.isEmpty(merchant_order_no.getText())) {
//            alert.setContentText("Please enter the merchant order number");
//            alert.showAndWait();
//            return "error";
//        }
//        switch (trans_choice.getValue()) {
//            case "Cash back": {
//                if (StrUtil.isEmpty(cashback_amount.getText())) {
//                    alert.setContentText("Please enter the cashback amount");
//                    alert.showAndWait();
//                    return "error";
//                }
//            }
//            case "Pre-authorization":
//            case "Sale": {
//                if (StrUtil.isEmpty(order_amount.getText())) {
//                    alert.setContentText("Please enter the order amount");
//                    alert.showAndWait();
//                    return "error";
//                }
//            }
//            return "ecrhub.pay.order";
//            case "Pre-auth Cancel":
//            case "Pre-auth Completion Cancel":
//            case "Cancel": {
//                if (StrUtil.isEmpty(orig_merchant_order_no.getText())) {
//                    alert.setContentText("Please enter the original merchant order number");
//                    alert.showAndWait();
//                    return "error";
//                }
//            }
//            return "ecrhub.pay.order";
//            case "Pre-auth Completion": {
//                if (StrUtil.isEmpty(orig_merchant_order_no.getText())) {
//                    alert.setContentText("Please enter the original merchant order number");
//                    alert.showAndWait();
//                    return "error";
//                }
//                if (StrUtil.isEmpty(order_amount.getText())) {
//                    alert.setContentText("Please enter the order amount");
//                    alert.showAndWait();
//                    return "error";
//                }
//            }
//            return "ecrhub.pay.order";
//
//            case "Query":
//                return "ecrhub.pay.query";
//            case "Close":
//                return "ecrhub.pay.close";
//        }
//        return "error";
//    }
//
//    @FXML
//    private void handleReturnButtonAction(ActionEvent event) {
//        SceneManager.getInstance().loadScene("home", "/com/example/ecrhub/fxml/home.fxml");
//        SceneManager.getInstance().switchScene("home");
//    }
//
//    @FXML
//    private void sendSampleMessageAction(ActionEvent event) {
//        if (StrUtil.isEmpty(DEBUG_PO.getSend_raw())) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("ERROR!");
//            alert.setContentText("Please create or input sample message");
//            alert.showAndWait();
//            return;
//        }
//
//        send_task = new Task<String>() {
//            @Override
//            protected String call() throws Exception {
//                create_message_button.setDisable(true);
//                send_message_button.setDisable(true);
//
//                wait_vbox.setVisible(true);
//                wait_vbox.setManaged(true);
//                receive_message.setVisible(false);
//                receive_message.setManaged(false);
//
//                byte[] send_bytes = HexUtil.decodeHex(DEBUG_PO.getSend_raw());
//                byte send_message_id = new SerialPortMessage().decode(send_bytes).getMessageId();
//                if (message_id > 0 && message_id == send_message_id) {
//                    Platform.runLater(() -> {
//                        Alert alert = new Alert(Alert.AlertType.ERROR);
//                        alert.setTitle("ERROR!");
//                        alert.setContentText("Cannot send the same message repeatedly!");
//                        alert.showAndWait();
//                    });
//                    return null;
//                }
//                message_id = send_message_id;
//
//                ECRHubClient client = ECRHubClientManager.getInstance().getClient();
//                ECRHubSerialPortClient portClient = (ECRHubSerialPortClient) client;
//                byte[] response_bytes;
//                try {
//                    response_bytes = portClient.send(REQUEST_ID, send_bytes);
//                } catch (Exception e) {
//                    DEBUG_PO.setReceive_raw(e.getMessage());
//                    DEBUG_PO.setReceive_pretty(e.getMessage());
//                    logger.error(e.getMessage(), e);
//                    throw e;
//                }
//                if (response_bytes == null || response_bytes.length == 0) {
//                    DEBUG_PO.setReceive_raw("");
//                    DEBUG_PO.setReceive_pretty("");
//                } else {
//                    DEBUG_PO.setReceive_raw(HexUtil.encodeHexStr(response_bytes, false));
//                    DEBUG_PO.setReceive_pretty(new SerialPortMessage().decode(response_bytes).toString());
//                }
//
//                receive_message.setText(receive_raw.isSelected() ? DEBUG_PO.getReceive_raw() : DEBUG_PO.getReceive_pretty());
//                return "success";
//            }
//        };
//
//        send_task.setOnSucceeded(success -> {
//            wait_vbox.setVisible(false);
//            wait_vbox.setManaged(false);
//            receive_message.setVisible(true);
//            receive_message.setManaged(true);
//
//            create_message_button.setDisable(false);
//            send_message_button.setDisable(false);
//        });
//
//        send_task.setOnFailed(fail -> {
//            wait_vbox.setVisible(false);
//            wait_vbox.setManaged(false);
//            receive_message.setVisible(true);
//            receive_message.setManaged(true);
//
//            create_message_button.setDisable(false);
//            send_message_button.setDisable(false);
//
//            Platform.runLater(() -> {
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("ERROR!");
//                alert.setContentText("Request to ECR-Hub error!");
//                alert.showAndWait();
//            });
//        });
//
//        send_task.setOnCancelled(cancel -> {
//            wait_vbox.setVisible(false);
//            wait_vbox.setManaged(false);
//            receive_message.setVisible(true);
//            receive_message.setManaged(true);
//
//            create_message_button.setDisable(false);
//            send_message_button.setDisable(false);
//        });
//
//        Thread thread = new Thread(send_task);
//        thread.start();
//    }
//
//    @FXML
//    private void sendCancelAction(ActionEvent event) {
//        if (send_task != null && send_task.isRunning()) {
//            send_task.cancel();
//        }
//    }
//}
