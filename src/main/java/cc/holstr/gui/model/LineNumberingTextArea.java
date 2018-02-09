package cc.holstr.gui.model;

import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;

public class LineNumberingTextArea extends JTextArea
{
    private JTextArea textArea;

    public LineNumberingTextArea(JTextArea textArea)
    {
        this.textArea = textArea;
        setBackground(Color.LIGHT_GRAY);
        setEditable(false);
    }

    public void updateLineNumbers()
    {
        String lineNumbersText = getLineNumbersText();
        setText(lineNumbersText);
    }

    private String getLineNumbersText()
    {
        int caretPosition = textArea.getDocument().getLength();
        Element root = textArea.getDocument().getDefaultRootElement();
        StringBuilder lineNumbersTextBuilder = new StringBuilder();
        lineNumbersTextBuilder.append("1   ").append(System.lineSeparator());

        for (int elementIndex = 2; elementIndex < root.getElementIndex(caretPosition) + 2; elementIndex++)
        {
            lineNumbersTextBuilder.append(String.format("%-3d",elementIndex)).append(System.lineSeparator());
        }

        return lineNumbersTextBuilder.toString();
    }
}
