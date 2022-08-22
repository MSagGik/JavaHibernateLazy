package ru.msaggik.hibernate;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.msaggik.hibernate.model.Item;
import ru.msaggik.hibernate.model.Person;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
    public static void main( String[] args ) {
        // подключение файла конфигурации hibernate.properties и классов Person и Item
        Configuration configuration = new Configuration()
                .addAnnotatedClass(Person.class)
                .addAnnotatedClass(Item.class);
        // создание сессии из configuration
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        // сессия
        Session session = sessionFactory.getCurrentSession();

        try {
            // начало транзакции
            session.beginTransaction();

            // вывод товаров пользователя:
            // 1) вывод пользователя с id=1
            Person person = session.get(Person.class, 1);
            // 2) вывод товаров данного пользователя
            System.out.println(person.getItems());
            // или
            // подгружение связанных сущностей с помощью специального метода
            Hibernate.initialize(person.getItems());

            // закрытие транзакции
            session.getTransaction().commit();
            // при ленивой загрузке вне сессии данный объект подгрузится
            // только в случае вызова его в самой сессии
            System.out.println(person.getItems());

            // открытие новой сессии
            session = sessionFactory.getCurrentSession(); // не смотря на одинаковое название
            // она другая и объекты подгруженные к старой сессии нужно снова подгружать
            // с помощью метода merge()
            // открытие новой транзакции
            session.beginTransaction();

            // подгружение объекта person
            person = (Person) session.merge(person);
            // два варианта подгрузки связанных сущностей:
            // 1) методом hibernate
            Hibernate.initialize(person.getItems());
            // 2) вручную запросом в БД
            List<Item> items = session.createQuery("SELECT i FROM Item i WHERE i.owner.id=:personId", Item.class)
                    .setParameter("personId", person.getId()).getResultList();
            System.out.println(items);

            // закрытие транзакции
            session.getTransaction().commit();

        } finally {
            // закрытие сессии
            sessionFactory.close();
        }
    }
}