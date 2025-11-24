/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.models.storage;

import core.models.*;
import java.util.ArrayList;
import java.util.List;

public class Storage {

    // Singleton
    private static Storage instance;

    private List<Stand> stands;
    private List<Person> persons;
    private List<Publisher> publishers;
    private List<Book> books;
    private final List<StorageObserver> observers = new ArrayList<>();

    private Storage() {
        this.stands = new ArrayList<>();
        this.persons = new ArrayList<>();
        this.publishers = new ArrayList<>();
        this.books = new ArrayList<>();
    }

    // Instancia Singleton
    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    public void addObserver(StorageObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(StorageObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (StorageObserver o : observers) {
            o.onStorageChanged();
        }
    }

    public List<Stand> getStands() {
        return stands;
    }

    public void addStand(Stand stand) {
        this.stands.add(stand);
        notifyObservers();
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void addPerson(Person person) {
        this.persons.add(person);
        notifyObservers();
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public void addPublisher(Publisher publisher) {
        this.publishers.add(publisher);
        notifyObservers();
    }

    public List<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        this.books.add(book);
        notifyObservers();
    }

}
