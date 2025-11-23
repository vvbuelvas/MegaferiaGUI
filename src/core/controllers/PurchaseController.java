/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.util.Response;
import core.controllers.util.Status;
import core.models.Publisher;
import core.models.Stand;
import core.models.storage.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class PurchaseController {
     private final Storage storage;

    public PurchaseController() {
        this.storage = Storage.getInstance();
    }

  
     // Acá se asocian los stands seleccionados con las editoriales seleccionadas.

    public Response assignStandsToPublishers(String standsText, String publishersText) {

        if (standsText == null || standsText.trim().isEmpty()) {
            return new Response("Debes seleccionar al menos un stand.",
                    Status.BAD_REQUEST);
        }

        if (publishersText == null || publishersText.trim().isEmpty()) {
            return new Response("Debes seleccionar al menos una editorial.",
                    Status.BAD_REQUEST);
        }

        List<Stand> selectedStands = new ArrayList<>();
        String[] standLines = standsText.split("\\R"); 

        for (String line : standLines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            long standId;
            try {
                standId = Long.parseLong(line);
            } catch (NumberFormatException ex) {
                return new Response("El ID de stand \"" + line + "\" no es numérico.",
                        Status.BAD_REQUEST);
            }

            Stand found = null;
            for (Stand s : storage.getStands()) {
                if (s.getId() == standId) {
                    found = s;
                    break;
                }
            }

            if (found == null) {
                return new Response("No existe un stand con ID " + standId + ".",
                        Status.BAD_REQUEST);
            }

            selectedStands.add(found);
        }

        if (selectedStands.isEmpty()) {
            return new Response("No hay stands válidos seleccionados.",
                    Status.BAD_REQUEST);
        }

        List<Publisher> selectedPublishers = new ArrayList<>();
        String[] publisherLines = publishersText.split("\\R");

        for (String line : publisherLines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            int openIdx = line.lastIndexOf('(');
            int closeIdx = line.lastIndexOf(')');
            if (openIdx == -1 || closeIdx == -1 || closeIdx <= openIdx) {
                return new Response("Formato inválido de editorial: " + line,
                        Status.BAD_REQUEST);
            }

            String nit = line.substring(openIdx + 1, closeIdx).trim();

            Publisher found = null;
            for (Publisher p : storage.getPublishers()) {
                if (p.getNit().equals(nit)) {
                    found = p;
                    break;
                }
            }

            if (found == null) {
                return new Response("No existe una editorial con NIT " + nit + ".",
                        Status.BAD_REQUEST);
            }

            selectedPublishers.add(found);
        }

        if (selectedPublishers.isEmpty()) {
            return new Response("No hay editoriales válidas seleccionadas.",
                    Status.BAD_REQUEST);
        }

        for (Stand stand : selectedStands) {
            for (Publisher publisher : selectedPublishers) {
                stand.addPublisher(publisher);
                publisher.addStand(stand);
            }
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("stands", selectedStands);
        data.put("publishers", selectedPublishers);

        return new Response("Compra realizada y asociaciones creadas correctamente.",
                Status.CREATED,
                data);
    }
}
