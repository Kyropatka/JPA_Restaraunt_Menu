package com.gmail.deniska1406sme;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class App {
    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATest");
    static EntityManager em = emf.createEntityManager();
    public static void main( String[] args ) {
        Scanner sc = new Scanner(System.in);

        addPosition("dorado",200.0,400.0,20);
        addPosition("burger",250.0,400.0,0);
        addPosition("salmon",150.0,500.0,20);
        addPosition("shashlyk cniken",200.0,400.0,0);
        addPosition("shashlyk pig",200.0,500.0,0);
        addPosition("shashlyk beef",200.0,600.0,0);
        addPosition("king crab",200.0,1200.0,20);
        addPosition("tiger shrimp",250.0,300.0,0);

        try {
            while (true) {
                System.out.println("1: add position");
                System.out.println("2: update position");
                System.out.println("3: delete apartment");
                System.out.println("4: view all positions");
                System.out.println("5: view positions by parameters");
                System.out.print("-> ");

                String s = sc.nextLine();
                switch (s) {
                    case "1":
                        addPosition(sc);
                        break;
                    case "2":
                        updatePosition(sc);
                        break;
                    case "3":
                        removePosition(sc);
                        break;
                    case "4":
                        getAllPositions();
                        break;
                    case "5":
                        getPositionByParameters(sc);
                        break;
                    default:
                        return;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void addPosition(Scanner sc){
        System.out.print("Enter the name of the position: ");
        String name = sc.nextLine();
        System.out.print("Enter weight of the position in gr: ");
        Double weight = sc.nextDouble();
        System.out.print("Enter price of the position: ");
        Double price = sc.nextDouble();
        System.out.print("Enter discount of the position: ");
        Integer discount = sc.nextInt();

        em.getTransaction().begin();
        try {
            Menu menu = new Menu(name, weight, price, discount);
            em.persist(menu);
            em.getTransaction().commit();
        }catch (Exception e){
            em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    public static void addPosition(String name, Double weight, Double price, Integer discount){
        em.getTransaction().begin();
        try {
            Menu menu = new Menu(name, weight, price, discount);
            em.persist(menu);
            em.getTransaction().commit();
        }catch (Exception e){
            em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    public static void removePosition(Scanner sc){
        System.out.print("Enter the name of the position: ");
        String name = sc.nextLine();

        em.getTransaction().begin();
        try {
            Menu menu = em.find(Menu.class, name);
            em.remove(menu);
            em.getTransaction().commit();
        }catch (Exception e){
            em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    public static void updatePosition(Scanner sc){
        System.out.print("Enter the name of the position: ");
        String name = sc.nextLine();
        System.out.print("Enter weight of the position in gr or leave blank: ");
        String weightInput = sc.nextLine();
        Double weight = weightInput.isEmpty() ? null : Double.parseDouble(weightInput);
        System.out.print("Enter price of the position or leave blank: ");
        String priceInput = sc.nextLine();
        Double price = priceInput.isEmpty() ? null : Double.parseDouble(priceInput);
        System.out.print("Enter discount of the position or leave blank: ");
        String discountInput = sc.nextLine();
        Integer discount = discountInput.isEmpty() ? null : Integer.parseInt(discountInput);


        em.getTransaction().begin();
        try {
            Menu menu = em.find(Menu.class, name);
            if(weight != null) menu.setWeight(weight);
            if(price != null) menu.setPrice(price);
            if(discount != null) menu.setDiscount(discount);

            em.merge(menu);
            em.getTransaction().commit();
        }catch (Exception e){
            em.getTransaction().rollback();
            e.printStackTrace();
        }
    }

    public static void getAllPositions(){
        em.getTransaction().begin();
        TypedQuery<Menu> query = em.createQuery("select m from Menu m", Menu.class);
        List<Menu> menus = query.getResultList();

        for (Menu menu : menus) {
            System.out.println(menu);
        }
    }

    public static void getPositionByParameters(Scanner sc){
        System.out.print("Enter minimum price(number or leave blank): ");
        String minPriceInput = sc.nextLine();
        Double minPrice = minPriceInput.isEmpty() ? null : Double.parseDouble(minPriceInput);

        System.out.print("Enter maximum price(number or leave blank): ");
        String maxPriceInput = sc.nextLine();
        Double maxPrice = maxPriceInput.isEmpty() ? null : Double.parseDouble(maxPriceInput);

        System.out.print("Choose only positions with discount?(y/n): ");
        String discount = sc.nextLine();
        if(discount.equalsIgnoreCase("n") || discount.isEmpty()){
            discount = null;
        }

        System.out.print("Make a set?(total weight no more than 1 kg) (y/n): ");
        String set = sc.nextLine();

        TypedQuery<Menu> query = em.createQuery(getQuery(minPrice,maxPrice,discount));
        List<Menu> res = query.getResultList();
        if (set.equalsIgnoreCase("n") || set.isEmpty()){
            for (Menu menu : res) {
                System.out.println(menu);
            }
        }else{
            List<Menu> res1 = getSet(res);
            for (Menu menu : res1) {
                System.out.println(menu);
            }
        }
    }

    static public CriteriaQuery<Menu> getQuery(Double minPrice, Double maxPrice, String discount){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Menu> query = cb.createQuery(Menu.class);
        Root<Menu> menu = query.from(Menu.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (discount != null) predicates.add(cb.gt(menu.get("discount"), 0));
        if (minPrice != null) predicates.add(cb.ge(menu.get("price"), minPrice));
        if (maxPrice != null) predicates.add(cb.le(menu.get("price"), maxPrice));

        query.select(menu).where(predicates.toArray(new Predicate[0]));

        return query;
    }

    static public List<Menu> getSet(List<Menu> positions){
        Double totalPrice = 0.0;
        Double totalWeight = 0.0;
        int skip;
        List<Menu> menuSet = new ArrayList<>();
        Random rn = new Random();
        int startIndex = rn.nextInt(positions.size());
        boolean firstIteration = true;

        while (totalWeight != 1000.00){
            skip = 0;
            int currentIndex = firstIteration ? startIndex : 0;

            for(int i = 0; i <positions.size();i++){
                Menu menu = positions.get((currentIndex + i) % positions.size());
                if ((totalWeight + menu.getWeight()) <= 1000.00){
                    totalWeight += menu.getWeight();
                    totalPrice += menu.getPrice();
                    menuSet.add(menu);
                }else {
                    skip += 1;
                }
            }
            if (skip == positions.size()){
                break;
            }
            firstIteration = false;
        }
        System.out.println("Total weight: " + totalWeight + ", total price: " + totalPrice);
        return menuSet;
    }
}
