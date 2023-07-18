package com.example.generateliquibase;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class KeyboardTranslator extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        Document document = editor.getDocument();
        SelectionModel selectionModel = editor.getSelectionModel();

        String selectedText = selectionModel.getSelectedText();

        if (selectedText != null) {
            String translatedText = translateToEnglish(selectedText);

            int startOffset = selectionModel.getSelectionStart();
            int endOffset = selectionModel.getSelectionEnd();
            System.out.println(translatedText);
            WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                document.replaceString(startOffset, endOffset, translatedText);
            });
        } else {
            Messages.showMessageDialog("Selection is empty, please select some text.", "Keyboard Translator", Messages.getInformationIcon());
        }
    }

    private static String translateToEnglish(String text) {
        String russianChars = "йцукенгшщзхъфывапролджэячсмитьбю";
        String englishChars = "qwertyuiop[]asdfghjkl;'zxcvbnm,.";
        StringBuilder translatedText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            int index = -1;

            if (russianChars.contains(Character.toString(currentChar))) {
                index = russianChars.indexOf(currentChar);
                if (index != -1) {
                    translatedText.append(englishChars.charAt(index));
                    continue;
                }
            } else if (englishChars.contains(Character.toString(currentChar))) {
                index = englishChars.indexOf(currentChar);
                if (index != -1) {
                    translatedText.append(russianChars.charAt(index));
                    continue;
                }
            }

            translatedText.append(currentChar);
        }

        return translatedText.toString();
    }


    @Override
    public boolean isDumbAware() {
        return super.isDumbAware();
    }
}
