package com.example.generateliquibase;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateLiquibaseAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        
        Project project = event.getProject();

        String liquibaseScript = generateLiquibaseScript();

        saveLiquibaseScript(project, liquibaseScript);
    }

    private String generateLiquibaseScript() {
        return "<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd\">\n" +
                "    <changeSet author=\"alakey (generated)\" id=\"77777777-9\">\n" +
                "        <createTable tableName=\"dsp_letter_template\">\n" +
                "            <column name=\"id\" type=\"UUID\">\n" +
                "                <constraints nullable=\"false\" primaryKey=\"true\" primaryKeyName=\"test_pkey\"/>\n" +
                "            </column>\n" +
                "            <column name=\"create_ts\" type=\"TIMESTAMP WITHOUT TIME ZONE\"/>\n" +
                "            <column name=\"created_by\" type=\"VARCHAR(50)\"/>\n" +
                "            <column name=\"delete_ts\" type=\"TIMESTAMP WITHOUT TIME ZONE\"/>\n" +
                "            <column name=\"deleted_by\" type=\"VARCHAR(50)\"/>\n" +
                "            <column name=\"update_ts\" type=\"TIMESTAMP WITHOUT TIME ZONE\"/>\n" +
                "            <column name=\"updated_by\" type=\"VARCHAR(50)\"/>\n" +
                "        </createTable>\n" +
                "    </changeSet>\n" +
                "</databaseChangeLog>";
    }

    private void saveLiquibaseScript(Project project, String script) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        VirtualFile baseDir = project.getBaseDir();
        VirtualFile selectedFile = FileChooser.chooseFile(descriptor, project, baseDir);
        if (selectedFile != null) {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String fileName = timestamp + "_changelog.xml";

            VirtualFile parentDir = selectedFile.getParent();
            VirtualFile file;
            try {
                file = parentDir.createChildData(this, fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (Writer writer = new FileWriter(file.getPath())) {
                writer.write(script);
                writer.flush();

                Messages.showInfoMessage("Liquibase script generated and saved successfully.", "Success");
            } catch (IOException e) {
                Messages.showErrorDialog("Failed to save Liquibase script: " + e.getMessage(), "Error");
            }
        }
    }
}
