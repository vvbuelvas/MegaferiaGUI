/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.util.Response;
import core.controllers.util.Status;
import core.models.Manager;
import core.models.Person;
import core.models.Publisher;
import core.models.storage.Storage;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

public class PublisherController {
     private final Storage storage;

    public PublisherController() {
        this.storage = Storage.getInstance();
    }

    public Response createPublisher(String nit,
                                    String name,
                                    String address,
                                    long managerId) {

        nit = nit == null ? "" : nit.trim();
        name = name == null ? "" : name.trim();
        address = address == null ? "" : address.trim();

        // Validacion para CAMPOS OBLIGATORIOS
        if (nit.isEmpty() || name.isEmpty() || address.isEmpty()) {
            return new Response("Todos los campos de la editorial son obligatorios.",
                    Status.BAD_REQUEST);
        }

        // Validacion formato XXX.XXX.XXX-X
        if (!nit.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{1}")) {
            return new Response("El NIT debe tener el formato XXX.XXX.XXX-X.",
                    Status.BAD_REQUEST);
        }

        // Validacion para NIT unico
        List<Publisher> publishers = storage.getPublishers();
        for (Publisher p : publishers) {
            if (p.getNit().equals(nit)) {
                return new Response("Ya existe una editorial con ese NIT.",
                        Status.BAD_REQUEST);
            }
        }

        // Buscar manager y su ID validando de que exista
        Manager manager = null;
        for (Person person : storage.getPersons()) {
            if (person.getId() == managerId && person instanceof Manager) {
                manager = (Manager) person;
                break;
            }
        }

        if (manager == null) {
            return new Response("El gerente seleccionado no es válido.",
                    Status.BAD_REQUEST);
        }


        if (manager.getPublisher() != null) {
            return new Response("Ese gerente ya tiene una editorial asignada.",
                    Status.BAD_REQUEST);
        }

        // Acà creamos la editorial y la enlazamos con el gerente
        Publisher publisher = new Publisher(nit, name, address, manager);
        storage.addPublisher(publisher);
        manager.setPublisher(publisher);

        HashMap<String, Object> data = new HashMap<>();
        data.put("publisher", publisher);

        return new Response("Editorial creada correctamente.",
                Status.CREATED,
                data);
    }
    
    public Response getAllPublishers() {
    // Llamamos editoriales desde el Storage
    List<Publisher> publishers = storage.getPublishers();

    ArrayList<HashMap<String, Object>> publishersList = new ArrayList<>();

    for (Publisher p : publishers) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("nit", p.getNit());
        map.put("name", p.getName());
        map.put("address", p.getAddress());

        String managerName = "-";
        if (p.getManager() != null) {
            managerName = p.getManager().getFullname();
        }
        map.put("managerName", managerName);

        map.put("standQuantity", p.getStandQuantity());

        publishersList.add(map);
    }

    HashMap<String, Object> data = new HashMap<>();
    data.put("publishers", publishersList);

    return new Response(
            "Editoriales obtenidas correctamente.",
            Status.OK,
            data
    );
}

}
