/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.controllers;

import core.controllers.util.Response;
import core.controllers.util.Status;
import core.models.Author;
import core.models.Audiobook;
import core.models.Book;
import core.models.DigitalBook;
import core.models.Narrator;
import core.models.PrintedBook;
import core.models.Publisher;
import core.models.Person;
import core.models.storage.Storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BookController {

    private final Storage storage;

    public BookController() {
        this.storage = Storage.getInstance();
    }

    public Response createBook(
            String title,
            String authorsText,
            String isbn,
            String genre,
            String format,
            String valueText,
            String publisherText,
            String pagesText,
            String copiesText,
            String hyperlink,
            String durationText,
            String narratorText,
            boolean isPrinted,
            boolean isDigital,
            boolean isAudio
    ) {

        if (title == null || title.trim().isEmpty()) {
            return new Response("El título es obligatorio.", Status.BAD_REQUEST);
        }

        if (authorsText == null || authorsText.trim().isEmpty()) {
            return new Response("Debes agregar al menos un autor.", Status.BAD_REQUEST);
        }

        if (isbn == null || isbn.trim().isEmpty()) {
            return new Response("El ISBN es obligatorio.", Status.BAD_REQUEST);
        }

        // ISBN: formato XXX-X-XX-XXXXXX-X
        String isbnTrim = isbn.trim();
        if (!isbnTrim.matches("\\d{3}-\\d-\\d{2}-\\d{6}-\\d")) {
            return new Response(
                    "El ISBN debe seguir el formato XXX-X-XX-XXXXXX-X (solo dígitos).",
                    Status.BAD_REQUEST
            );
        }

        // ISBN único
        for (Book b : storage.getBooks()) {
            if (b.getIsbn().equals(isbnTrim)) {
                return new Response("Ya existe un libro con ese ISBN.", Status.BAD_REQUEST);
            }
        }

        if (genre == null || genre.startsWith("Seleccione")) {
            return new Response("Debes seleccionar un género.", Status.BAD_REQUEST);
        }

        if (format == null || format.startsWith("Seleccione")) {
            return new Response("Debes seleccionar un formato.", Status.BAD_REQUEST);
        }

        if (publisherText == null || publisherText.startsWith("Seleccione")) {
            return new Response("Debes seleccionar una editorial.", Status.BAD_REQUEST);
        }

        if (!isPrinted && !isDigital && !isAudio) {
            return new Response(
                    "Debes seleccionar un tipo de libro (Impreso, Digital o Audio Libro).",
                    Status.BAD_REQUEST
            );
        }

        double value;
        try {
            value = Double.parseDouble(valueText.trim());
        } catch (NumberFormatException ex) {
            return new Response("El valor del libro debe ser numérico.", Status.BAD_REQUEST);
        }

        if (value <= 0) {
            return new Response(
                    "El valor de los libros debe ser superior a 0.",
                    Status.BAD_REQUEST
            );
        }

        ArrayList<Author> authors = new ArrayList<>();
        String[] authorLines = authorsText.split("\\R");
        HashSet<Long> authorIds = new HashSet<>();

        for (String line : authorLines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(" - ");
            if (parts.length < 1) {
                return new Response("Formato de autor inválido: " + line, Status.BAD_REQUEST);
            }

            long authorId;
            try {
                authorId = Long.parseLong(parts[0].trim());
            } catch (NumberFormatException ex) {
                return new Response(
                        "El ID de autor no es numérico: " + parts[0],
                        Status.BAD_REQUEST
                );
            }

            Author found = null;
            for (Person person : storage.getPersons()) {
                if (person instanceof Author) {
                    Author a = (Author) person;
                    if (a.getId() == authorId) {
                        found = a;
                        break;
                    }
                }
            }
            if (authorIds.contains(authorId)) {
                return new Response(
                        "No se permiten autores repetidos en un mismo libro.",
                        Status.BAD_REQUEST
                );
            }
            authorIds.add(authorId);

            if (found == null) {
                return new Response(
                        "No se encontró el autor con ID " + authorId + ".",
                        Status.BAD_REQUEST
                );
            }

            authors.add(found);
        }

        if (authors.isEmpty()) {
            return new Response(
                    "No se obtuvo ningún autor válido para el libro.",
                    Status.BAD_REQUEST
            );
        }

        int idxOpen = publisherText.lastIndexOf('(');
        int idxClose = publisherText.lastIndexOf(')');
        if (idxOpen == -1 || idxClose == -1 || idxClose <= idxOpen) {
            return new Response("Formato de editorial inválido.", Status.BAD_REQUEST);
        }

        String publisherNit = publisherText.substring(idxOpen + 1, idxClose).trim();

        Publisher publisher = null;
        for (Publisher p : storage.getPublishers()) {
            if (p.getNit().equals(publisherNit)) {
                publisher = p;
                break;
            }
        }

        if (publisher == null) {
            return new Response(
                    "No se encontró la editorial con NIT " + publisherNit + ".",
                    Status.BAD_REQUEST
            );
        }

        Book createdBook = null;

        if (isPrinted) {
            int pages;
            int copies;
            try {
                pages = Integer.parseInt(pagesText.trim());
                copies = Integer.parseInt(copiesText.trim());
            } catch (NumberFormatException ex) {
                return new Response(
                        "Páginas y ejemplares deben ser numéricos.",
                        Status.BAD_REQUEST
                );
            }

            createdBook = new PrintedBook(
                    title,
                    authors,
                    isbnTrim,
                    genre,
                    format,
                    value,
                    publisher,
                    pages,
                    copies
            );
        }

        if (isDigital) {
            if (hyperlink == null || hyperlink.trim().isEmpty()) {
                createdBook = new DigitalBook(
                        title,
                        authors,
                        isbnTrim,
                        genre,
                        format,
                        value,
                        publisher
                );
            } else {
                createdBook = new DigitalBook(
                        title,
                        authors,
                        isbnTrim,
                        genre,
                        format,
                        value,
                        publisher,
                        hyperlink.trim()
                );
            }
        }

        if (isAudio) {
            int duration;
            try {
                duration = Integer.parseInt(durationText.trim());
            } catch (NumberFormatException ex) {
                return new Response(
                        "La duración del audiolibro debe ser numérica.",
                        Status.BAD_REQUEST
                );
            }

            if (narratorText == null || narratorText.startsWith("Seleccione")) {
                return new Response(
                        "Debes seleccionar un narrador para el audiolibro.",
                        Status.BAD_REQUEST
                );
            }

            // Narrador válido (debe existir previamente)
            String[] narratorParts = narratorText.split(" - ");
            if (narratorParts.length < 1) {
                return new Response(
                        "Formato de narrador inválido: " + narratorText,
                        Status.BAD_REQUEST
                );
            }

            long narratorId;
            try {
                narratorId = Long.parseLong(narratorParts[0].trim());
            } catch (NumberFormatException ex) {
                return new Response(
                        "El ID del narrador no es numérico.",
                        Status.BAD_REQUEST
                );
            }

            Narrator narrator = null;
            for (Person person : storage.getPersons()) {
                if (person instanceof Narrator n) {
                    if (n.getId() == narratorId) {
                        narrator = n;
                        break;
                    }
                }
            }

            if (narrator == null) {
                return new Response(
                        "No se encontró el narrador con ID " + narratorId + ".",
                        Status.BAD_REQUEST
                );
            }

            createdBook = new Audiobook(
                    title,
                    authors,
                    isbnTrim,
                    genre,
                    format,
                    value,
                    publisher,
                    duration,
                    narrator
            );
        }

        if (createdBook == null) {
            return new Response(
                    "No se pudo crear el libro. Verifica el tipo seleccionado.",
                    Status.BAD_REQUEST
            );
        }

        storage.addBook(createdBook);

        HashMap<String, Object> data = new HashMap<>();
        data.put("book", createdBook);

        return new Response("Libro creado correctamente.", Status.CREATED, data);
    }

    private HashMap<String, Object> mapBook(Book b) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("title", b.getTitle());
        map.put("isbn", b.getIsbn());
        map.put("genre", b.getGenre());
        map.put("format", b.getFormat());
        map.put("value", b.getValue());
        map.put("publisher", b.getPublisher().getName());

        String authorsNames = "-";
        if (b.getAuthors() != null && !b.getAuthors().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(b.getAuthors().get(0).getFullname());
            for (int i = 1; i < b.getAuthors().size(); i++) {
                sb.append(", ").append(b.getAuthors().get(i).getFullname());
            }
            authorsNames = sb.toString();
        }
        map.put("authors", authorsNames);

        map.put("copies", "-");
        map.put("pages", "-");
        map.put("url", "-");
        map.put("narrator", "-");
        map.put("duration", "-");

        switch (b) {
            case PrintedBook pb -> {
                map.put("copies", pb.getCopies());
                map.put("pages", pb.getPages());
            }
            case DigitalBook db -> {
                if (db.hasHyperlink()) {
                    map.put("url", db.getHyperlink());
                } else {
                    map.put("url", "No");
                }
            }
            case Audiobook ab -> {
                if (ab.getNarrador() != null) {
                    map.put("narrator", ab.getNarrador().getFullname());
                }
                map.put("duration", ab.getDuration());
            }
            default -> {
            }
        }

        return map;
    }

    public Response getAllBooks() {
        List<Book> books = new ArrayList<>(storage.getBooks());
        books.sort(Comparator.comparing(Book::getIsbn));

        ArrayList<HashMap<String, Object>> result = new ArrayList<>();

        for (Book b : books) {
            result.add(mapBook(b));
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("books", result);

        return new Response("Libros obtenidos correctamente.", Status.OK, data);
    }

    public Response getBooksByType(String type) {
        List<Book> books = new ArrayList<>(storage.getBooks());

        books = books.stream()
                .filter(b -> {
                    if (type.equalsIgnoreCase("impreso")) {
                        return b instanceof PrintedBook;
                    }
                    if (type.equalsIgnoreCase("digital")) {
                        return b instanceof DigitalBook;
                    }
                    if (type.equalsIgnoreCase("audio")) {
                        return b instanceof Audiobook;
                    }
                    return true; // TODOS
                })
                .sorted(Comparator.comparing(Book::getIsbn))
                .toList();

        ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        for (Book b : books) {
            result.add(mapBook(b));
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("books", result);

        return new Response("Libros filtrados correctamente.", Status.OK, data);
    }

    public Response getBooksForShowTab(String filter) {
        List<Book> allBooks = new ArrayList<>(storage.getBooks());

        // Filtrar por tipo
        List<Book> filtered = new ArrayList<>();
        for (Book b : allBooks) {
            boolean include;
            include = switch (filter) {
                case "IMPRESO" ->
                    b instanceof PrintedBook;
                case "DIGITAL" ->
                    b instanceof DigitalBook;
                case "AUDIO" ->
                    b instanceof Audiobook;
                default ->
                    true;
            }; // "TODOS"
            if (include) {
                filtered.add(b);
            }
        }

        filtered.sort(Comparator.comparing(Book::getIsbn));

        ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
        for (Book b : filtered) {
            dataList.add(mapBook(b));
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("books", dataList);

        return new Response("Libros obtenidos correctamente.", Status.OK, data);
    }

    private Author findAuthorById(long authorId) {
        for (Person p : storage.getPersons()) {
            if (p instanceof Author a && a.getId() == authorId) {
                return a;
            }
        }
        return null;
    }

    public Response findBooksByAuthor(long authorId) {
        Author author = findAuthorById(authorId);
        if (author == null) {
            return new Response("El autor seleccionado no es válido.",
                    Status.BAD_REQUEST);
        }

        List<Book> books = new ArrayList<>(storage.getBooks());
        List<Book> filtered = new ArrayList<>();

        for (Book b : books) {
            for (Author a : b.getAuthors()) {
                if (a.getId() == authorId) {
                    filtered.add(b);
                    break;
                }
            }
        }

        filtered.sort(Comparator.comparing(Book::getIsbn));

        ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
        for (Book b : filtered) {
            dataList.add(mapBook(b));
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("books", dataList);

        return new Response("Libros del autor obtenidos correctamente.",
                Status.OK, data);
    }

    public Response findBooksByAuthorAndFormat(long authorId, String format) {
        if (format == null || format.trim().isEmpty()
                || format.startsWith("Seleccione")) {
            return new Response("Debes seleccionar un formato válido.",
                    Status.BAD_REQUEST);
        }

        Author author = findAuthorById(authorId);
        if (author == null) {
            return new Response("El autor seleccionado no es válido.",
                    Status.BAD_REQUEST);
        }

        List<Book> books = new ArrayList<>(storage.getBooks());
        List<Book> filtered = new ArrayList<>();

        for (Book b : books) {
            if (!b.getFormat().equalsIgnoreCase(format.trim())) {
                continue;
            }
            for (Author a : b.getAuthors()) {
                if (a.getId() == authorId) {
                    filtered.add(b);
                    break;
                }
            }
        }

        filtered.sort(Comparator.comparing(Book::getIsbn));

        ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
        for (Book b : filtered) {
            dataList.add(mapBook(b));
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("books", dataList);

        return new Response("Libros del autor y formato obtenidos correctamente.",
                Status.OK, data);
    }

    public Response getAuthorsWithMoreBooksInDifferentPublishers() {

        class AuthorInfo {

            Author author;
            int bookCount = 0;
            java.util.HashSet<String> publisherNits = new java.util.HashSet<>();
        }

        HashMap<Long, AuthorInfo> infoMap = new HashMap<>();

        for (Book b : storage.getBooks()) {
            String nit = b.getPublisher().getNit();

            for (Author a : b.getAuthors()) {
                AuthorInfo info = infoMap.get(a.getId());
                if (info == null) {
                    info = new AuthorInfo();
                    info.author = a;
                    infoMap.put(a.getId(), info);
                }
                info.bookCount++;
                info.publisherNits.add(nit);
            }
        }

        // Filtrar autores con libros en ≥ 2 editoriales
        ArrayList<AuthorInfo> selected = new ArrayList<>();
        for (AuthorInfo info : infoMap.values()) {
            if (info.publisherNits.size() >= 2) {
                selected.add(info);
            }
        }

        selected.sort(Comparator.comparingLong(ai -> ai.author.getId()));

        ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
        for (AuthorInfo info : selected) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", info.author.getId());
            map.put("name", info.author.getFullname());
            map.put("count", info.bookCount);
            dataList.add(map);
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("authors", dataList);

        return new Response("Autores con más libros en diferentes editoriales obtenidos correctamente.",
                Status.OK, data);
    }

    public Response findBooksByFormat(String format) {
        if (format == null || format.trim().isEmpty()
                || format.startsWith("Seleccione")) {
            return new Response("Debes seleccionar un formato válido.",
                    Status.BAD_REQUEST);
        }

        List<Book> books = new ArrayList<>(storage.getBooks());
        List<Book> filtered = new ArrayList<>();

        for (Book b : books) {
            if (b.getFormat() != null
                    && b.getFormat().equalsIgnoreCase(format.trim())) {
                filtered.add(b);
            }
        }

        filtered.sort(Comparator.comparing(Book::getIsbn)); // orden por ISBN

        ArrayList<HashMap<String, Object>> dataList = new ArrayList<>();
        for (Book b : filtered) {
            dataList.add(mapBook(b));
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("books", dataList);

        return new Response(
                "Libros filtrados por formato obtenidos correctamente.",
                Status.OK,
                data
        );
    }

}
