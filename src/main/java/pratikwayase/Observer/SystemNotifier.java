package pratikwayase.Observer;

import java.util.*;


import java.util.ArrayList;
import java.util.List;

import java.util.*;

// Using a generic type for more flexible event notifications
public class SystemNotifier<T> {
    private final List<Observer<T>> observers = new ArrayList<>();

    public void addObserver(Observer<T> observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }

    public void notifyObservers(T event) {
        for (Observer<T> observer : observers) {
            observer.update(event);
        }
    }
}