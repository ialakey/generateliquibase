package com.example.generateliquibase;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.hibernate.mapping.Property;


public class GenerateLiquibaseAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
//        String liquibaseScript = generateLiquibaseScript(entityClass);
//        saveLiquibaseScript(project, liquibaseScript);
    }

    private String generateLiquibaseScript(Class<?> entityClass) {
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().configure().build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(entityClass);
        Metadata metadata = metadataSources.buildMetadata();

        PersistentClass persistentClass = metadata.getEntityBinding(entityClass.getName());

        StringBuilder liquibaseScript = new StringBuilder();
        liquibaseScript.append("<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        liquibaseScript.append("<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n");
        liquibaseScript.append("                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        liquibaseScript.append("                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.1.xsd\">\n");
        liquibaseScript.append("    <changeSet author=\"generated\" id=\"").append(System.currentTimeMillis()).append("\">\n");
        liquibaseScript.append("        <createTable tableName=\"").append(persistentClass.getTable().getName()).append("\">\n");
        liquibaseScript.append("            <column name=\"id\" type=\"UUID\">\n" +
                "                <constraints nullable=\"false\" primaryKey=\"true\" primaryKeyName=\"" + persistentClass.getTable().getName() +"\"/>\n" +
                "            </column>\n" +
                "            <column name=\"create_ts\" type=\"TIMESTAMP WITHOUT TIME ZONE\"/>\n" +
                "            <column name=\"created_by\" type=\"VARCHAR(50)\"/>\n" +
                "            <column name=\"delete_ts\" type=\"TIMESTAMP WITHOUT TIME ZONE\"/>\n" +
                "            <column name=\"deleted_by\" type=\"VARCHAR(50)\"/>\n" +
                "            <column name=\"update_ts\" type=\"TIMESTAMP WITHOUT TIME ZONE\"/>\n" +
                "            <column name=\"updated_by\" type=\"VARCHAR(50)\"/>");

        Iterator<Property> propertyIterator = persistentClass.getPropertyIterator();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();
            if (property.isComposite()) {
                continue;
            }
            Iterator<Column> columnIterator = property.getColumnIterator();
            if (columnIterator.hasNext()) {
                Column column = columnIterator.next();
                liquibaseScript.append("            <column name=\"").append(column.name()).append("\" type=\"").append(column.columnDefinition()).append("\"/>\n");
            }
        }

        liquibaseScript.append("        </createTable>\n");
        liquibaseScript.append("    </changeSet>\n");
        liquibaseScript.append("</databaseChangeLog>");

        String fileName = System.currentTimeMillis() + "_changelog.xml";
        try (Writer writer = new FileWriter(fileName)) {
            writer.write(liquibaseScript.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return liquibaseScript.toString();
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
