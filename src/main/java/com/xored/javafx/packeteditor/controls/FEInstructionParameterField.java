package com.xored.javafx.packeteditor.controls;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.xored.javafx.packeteditor.data.FEInstructionParameter;
import com.xored.javafx.packeteditor.data.FEInstructionParameter2;
import com.xored.javafx.packeteditor.data.InstructionExpression;
import com.xored.javafx.packeteditor.metatdata.FEInstructionParameterMeta;
import com.xored.javafx.packeteditor.scapy.ScapyException;
import com.xored.javafx.packeteditor.view.ComboBoxItem;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FEInstructionParameterField extends EditableField {
    
    protected FEInstructionParameter2 feInstructionParameter;

    protected InstructionExpression feInstruction;
    
    public void init(FEInstructionParameter2 option) {
        this.feInstructionParameter = option;
        switch (option.getType()) {
            case ENUM:
                this.editableControl = createEnumField();
                break;
            case NUMBER:
            case EXPRESSION:
            case STRING:
            default:
                this.editableControl = createTextField(); 
        }
        this.label = createLabel();
       
        getChildren().addAll(label);
    }

    protected Node createLabel() {
        Label valueNode = (Label) super.createLabel();

        String val = getInstructionParamValue();
        if (feInstructionParameter.getMeta().isEnum()) {
            val = Strings.isNullOrEmpty(val) ? "Not selected" : val;
            if(feInstructionParameter.getMeta().getDict() != null) {
                val = feInstructionParameter.getMeta().getDict().get(val);
            }
        }
        valueNode.setText(val);
        
        return valueNode;
    }
    
    @Override
    protected String getUniqueViewId() {
        return feInstructionParameter.getId();
    }

    @Override
    protected void revertTextFieldValue(TextField tf) {
        tf.setText(getInstructionParamValue());
    }

    @Override
    public ContextMenu getContextMenu() {
        return null;
    }

    @Override
    protected void onLableClickedAction(MouseEvent event) {
        if (!MouseButton.SECONDARY.equals(event.getButton())) {
            showControl();
        }
    }

    @Override
    protected String getLabelText() {
        return getInstructionParamValue();
    }

    @Override
    protected void processDefaultAndSetItems(ComboBox<ComboBoxItem> combo, List<ComboBoxItem> items) {
        combo.getItems().addAll(items);
        ComboBoxItem defaultVal = new ComboBoxItem(getInstructionParamValue(), feInstructionParameter.getValue());
        if (feInstructionParameter.getMeta().getDict() != null) {
            String comboItemVal = getInstructionParamValue();
            String comboItemLabel = feInstructionParameter.getMeta().getDict().get(comboItemVal);
            defaultVal = new ComboBoxItem(comboItemLabel, new JsonPrimitive(comboItemVal));
        }
        combo.setValue(defaultVal);
    }
    
    @Override
    protected List<ComboBoxItem> getComboBoxItems() {
        Map<String, String> parameterValues = controller.getModel().loadParameterValuesFromScapy(feInstructionParameter.getMeta());
        if (parameterValues.isEmpty()) {
            parameterValues = feInstructionParameter.getMeta().getDict() == null ? Collections.emptyMap() : feInstructionParameter.getMeta().getDict();
        }
        return parameterValues.entrySet().stream()
                .map(entry -> new ComboBoxItem(entry.getValue(), new JsonPrimitive(entry.getKey())))
                .collect(Collectors.toList());
    }

    @Override
    protected TextField getTextField() {
        return new TextField(getInstructionParamValue());
    }

    private String getInstructionParamValue() {
        String value;

        if (feInstructionParameter.getMeta().isExpression()) {
            JsonElement jsonElement = feInstructionParameter.getValue();
            value = jsonElement.isJsonObject()? jsonElement.getAsJsonObject().get("expr").getAsString() : jsonElement.getAsString();
        } else {
            value = feInstructionParameter.getValue().getAsString();
        }

        if (value == null) {
            value = feInstructionParameter.getDefaultValue();
        }
        return value;
    }
    
    @Override
    protected void commitChanges(ComboBox<ComboBoxItem> combo) {
        Object selectedItem = combo.getSelectionModel().getSelectedItem();
        String selection = selectedItem instanceof ComboBoxItem ? ((ComboBoxItem) selectedItem).getValue().getAsString() : selectedItem.toString();
        controller.getModel().setVmInstructionParameter(feInstructionParameter, FEInstructionParameter2.createHumanValue(selection));
    }

    @Override
    protected void commitChanges(TextField textField) {
        JsonElement prevValue = feInstructionParameter.getValue();
        try {
            if (feInstructionParameter.getType() == FEInstructionParameterMeta.Type.EXPRESSION) {
                controller.getModel().setVmInstructionParameter(feInstructionParameter, FEInstructionParameter2.createExpressionValue(textField.getText()));
            } else {
                controller.getModel().setVmInstructionParameter(feInstructionParameter, FEInstructionParameter2.createHumanValue(textField.getText()));
            }
        } catch (ScapyException e) {
            textField.getStyleClass().add("field-error");
            controller.getModel().getUserModel().setFEInstructionParameter(feInstructionParameter, prevValue);
        }
    }

    @Override
    protected String getLabelCssClass() {
        return "field-value-default";
    }
    
    @Override
    protected boolean isCheckBoxEditable() {
        return feInstructionParameter.editable();
    }

    @Override
    protected void onComboBoxSelectedAction(ComboBox<ComboBoxItem> combo) {
        commitChanges(combo);
    }

    public void setInstruction(InstructionExpression instruction) {
        this.feInstruction = instruction;
    }
}
