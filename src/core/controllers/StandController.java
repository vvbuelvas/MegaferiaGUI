package core.controllers;

import core.controllers.util.Response;
import core.controllers.util.Status;
import core.models.Stand;
import core.models.storage.Storage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StandController {

    public Response createStand(String idText, String priceText) {
        long id;
        double price;

        // Algunas validaciones basicas
        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException ex) {
            return new Response("El ID del stand debe ser numérico.", Status.BAD_REQUEST);
        }

        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException ex) {
            return new Response("El precio debe ser numérico.", Status.BAD_REQUEST);
        }

        // Validaciones del parcial para controlador Stand
        if (id < 0) {
            return new Response("El ID del stand debe ser mayor o igual a 0.", Status.BAD_REQUEST);
        }

        if (String.valueOf(id).length() > 15) {
            return new Response("El ID del stand no puede tener más de 15 dígitos.", Status.BAD_REQUEST);
        }

        if (price <= 0) {
            return new Response("El precio del stand debe ser mayor que 0.", Status.BAD_REQUEST);
        }

        // Validacion de repetecion del ID
        List<Stand> stands = Storage.getInstance().getStands();
        for (Stand stand : stands) {
            if (stand.getId() == id) {
                return new Response("Ya existe un stand con ese ID.", Status.BAD_REQUEST);
            }
        }

        // Se Crea Stand
        Stand stand = new Stand(id, price);
        Storage.getInstance().addStand(stand);

        HashMap<String, Object> data = new HashMap<>();
        data.put("stand", stand);

        return new Response("Stand creado correctamente.", Status.CREATED, data);
    }

    public Response getAllStands() {

        List<Stand> stands = new ArrayList<>(Storage.getInstance().getStands());
        stands.sort(java.util.Comparator.comparingLong(Stand::getId));

        ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();

        for (Stand stand : stands) {
            String publishersNames = "";
            if (stand.getPublisherQuantity() > 0) {
                publishersNames += stand.getPublishers().get(0).getName();
                for (int i = 1; i < stand.getPublisherQuantity(); i++) {
                    publishersNames += ", " + stand.getPublishers().get(i).getName();
                }
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put("id", stand.getId());
            map.put("price", stand.getPrice());
            map.put("comprado", stand.getPublisherQuantity() > 0 ? "Si" : "No");
            map.put("publishers", publishersNames);

            dataList.add(map);
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("stands", dataList);

        return new Response("Stands obtenidos correctamente.", Status.OK, data);
    }
}
