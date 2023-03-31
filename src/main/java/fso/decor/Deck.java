package fso.decor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Deck {
    HashMap<Integer, HashSet<Card>> pageToCards;
    HashMap<Integer, Card> idToCard;
    static int id;
    static final String delimiter = "‽";
    private final String pdfHash;
    private final GlobalConfig config;

    public Deck(String pdfHash) {
        this.pdfHash = pdfHash;
        config = GlobalConfig.getInstance(pdfHash);
        pageToCards = new HashMap<>();
        idToCard = new HashMap<>();
        id = 1;
        File deckFile = new File(GlobalConfig.getImageFolder(), pdfHash + ".deck");
        if (deckFile.exists()) {
            try (Scanner reader = new Scanner(deckFile)) {
                id = Integer.parseInt(reader.nextLine());
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    addCardFromSerialiseString(line);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addCard(String front, String back, int beginningPage, double beginningPercentage, int endPage,
                           double endPercentage) {
        addCardWithId(front, back, beginningPage, beginningPercentage, endPage, endPercentage, id++);
    }

    public void addCardWithId(String front, String back, int beginningPage, double beginningPercentage, int endPage,
                        double endPercentage, int id) {
        assert beginningPage <= endPage;
        Card card = new Card(front, back, beginningPage, beginningPercentage, endPage, endPercentage, id, pdfHash);
        for (int page = beginningPage; page <= endPage; page++) {
            pageToCards.putIfAbsent(page, new HashSet<>());
            pageToCards.get(page).add(card);
        }
        idToCard.put(id, card);
    }

    public void addCardFromSerialiseString(String serialiseString) {
        // front, back, beginningPage, beginningPercentage, endPage, endPercentage, id
        String[] elements = serialiseString.split(delimiter);
        assert elements.length == 7;
        String front = elements[0];
        String back = elements[1];
        int beginningPage = Integer.parseInt(elements[2]);
        double beginningPercentage = Double.parseDouble(elements[3]);
        int endPage = Integer.parseInt(elements[4]);
        double endPercentage = Double.parseDouble(elements[5]);
        int id = Integer.parseInt(elements[6]);
        addCardWithId(front, back, beginningPage, beginningPercentage, endPage, endPercentage, id);
    }

    public ArrayList<Double> renderScrollBar(int page) {
        // Return {beg1, end1, beg2, end2, ...} for red
        if (!pageToCards.containsKey(page) || pageToCards.get(page).isEmpty())
            return new ArrayList<>();

        ArrayList<Double> ret = new ArrayList<>();
        for (Card card : pageToCards.get(page)) {
            double beg = page == card.getBeginningPage() ? card.getBeginningPercentage() : 0;
            double end = page == card.getEndPage() ? card.getEndPercentage() : 1;
            ret.add(beg);
            ret.add(end);
        }
        assert ret.size() % 2 == 0;
        return ret;
    }

    public Set<Card> getCardsInPosition(int page, double position) {
        return pageToCards.get(page).stream().filter(card -> card.intersects(page, position))
                .collect(Collectors.toSet());
    }

    public Set<Integer> deleteCard(int id) {
        // returns set of pages to be refreshed
        Card card = idToCard.get(id);
        idToCard.remove(id);
        Set<Integer> ret = new HashSet<>();
        for (int page = card.getBeginningPage(); page <= card.getEndPage(); page++) {
            pageToCards.get(page).remove(card);
            ret.add(page);
        }
        return ret;
    }

    public String getSerialiseString() {
        // id\ncard1\ncard2...
        StringBuilder ret = new StringBuilder();
        ret.append(id + "\n");
        for (Card card : idToCard.values()) {
            ret.append(card.getSerialiseString(delimiter) + "\n");
        }
        return ret.toString();
    }

    public Collection<Card> getCards() {
        return idToCard.values();
    }

    public HashMap<Integer, HashSet<Card>> getPageToCards() {
        return pageToCards;
    }
}
