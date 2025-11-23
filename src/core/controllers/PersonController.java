/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.util.Response;
import core.controllers.util.Status;
import core.models.Author;
import core.models.Manager;
import core.models.Narrator;
import core.models.Person;
import core.models.storage.Storage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class PersonController {
     private final Storage storage;

    public PersonController() {
        this.storage = Storage.getInstance();
    }

    public Response createAuthor(String idText, String firstname, String lastname) {
        long id;

        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException ex) {
            return new Response("El ID de la persona debe ser numérico.", Status.BAD_REQUEST);
        }

        if (id < 0) {
            return new Response("El ID de la persona debe ser mayor o igual a 0.", Status.BAD_REQUEST);
        }

        if (String.valueOf(id).length() > 15) {
            return new Response("El ID de la persona no puede tener más de 15 dígitos.", Status.BAD_REQUEST);
        }

        firstname = firstname == null ? "" : firstname.trim();
        lastname  = lastname  == null ? "" : lastname.trim();

        if (firstname.isEmpty() || lastname.isEmpty()) {
            return new Response("Nombre y apellido son obligatorios.", Status.BAD_REQUEST);
        }

        for (Person p : storage.getPersons()) {
            if (p.getId() == id) {
                return new Response("Ya existe una persona con ese ID.", Status.BAD_REQUEST);
            }
        }

        Author author = new Author(id, firstname, lastname);
        storage.addPerson(author);

        HashMap<String, Object> data = new HashMap<>();
        data.put("person", author);

        return new Response("Autor creado correctamente.", Status.CREATED, data);
    }

    
    
    
    public Response createManager(String idText, String firstname, String lastname) {
        long id;

        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException ex) {
            return new Response("El ID de la persona debe ser numérico.", Status.BAD_REQUEST);
        }

        if (id < 0) {
            return new Response("El ID de la persona debe ser mayor o igual a 0.", Status.BAD_REQUEST);
        }

        if (String.valueOf(id).length() > 15) {
            return new Response("El ID de la persona no puede tener más de 15 dígitos.", Status.BAD_REQUEST);
        }

        firstname = firstname == null ? "" : firstname.trim();
        lastname  = lastname  == null ? "" : lastname.trim();

        if (firstname.isEmpty() || lastname.isEmpty()) {
            return new Response("Nombre y apellido son obligatorios.", Status.BAD_REQUEST);
        }

        for (Person p : storage.getPersons()) {
            if (p.getId() == id) {
                return new Response("Ya existe una persona con ese ID.", Status.BAD_REQUEST);
            }
        }

        Manager manager = new Manager(id, firstname, lastname);
        storage.addPerson(manager);

        HashMap<String, Object> data = new HashMap<>();
        data.put("person", manager);

        return new Response("Gerente creado correctamente.", Status.CREATED, data);
    }


    public Response createNarrator(String idText, String firstname, String lastname) {
        long id;

        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException ex) {
            return new Response("El ID de la persona debe ser numérico.", Status.BAD_REQUEST);
        }

        if (id < 0) {
            return new Response("El ID de la persona debe ser mayor o igual a 0.", Status.BAD_REQUEST);
        }

        if (String.valueOf(id).length() > 15) {
            return new Response("El ID de la persona no puede tener más de 15 dígitos.", Status.BAD_REQUEST);
        }

        firstname = firstname == null ? "" : firstname.trim();
        lastname  = lastname  == null ? "" : lastname.trim();

        if (firstname.isEmpty() || lastname.isEmpty()) {
            return new Response("Nombre y apellido son obligatorios.", Status.BAD_REQUEST);
        }

        for (Person p : storage.getPersons()) {
            if (p.getId() == id) {
                return new Response("Ya existe una persona con ese ID.", Status.BAD_REQUEST);
            }
        }

        Narrator narrator = new Narrator(id, firstname, lastname);
        storage.addPerson(narrator);

        HashMap<String, Object> data = new HashMap<>();
        data.put("person", narrator);

        return new Response("Narrador creado correctamente.", Status.CREATED, data);
    }

    
    
    
    public List<Person> getPersonsOrderedById() {
        List<Person> persons = new ArrayList<>(storage.getPersons());
        persons.sort(Comparator.comparingLong(Person::getId));
        return persons;
    }
}
