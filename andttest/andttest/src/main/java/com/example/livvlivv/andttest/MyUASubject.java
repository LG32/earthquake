package com.example.livvlivv.andttest;

public interface MyUASubject {
     void registerObserver(MyUAObServer o);
     void removeObserver(MyUAObServer o);
     void notifyObserver(String mydata);
}
